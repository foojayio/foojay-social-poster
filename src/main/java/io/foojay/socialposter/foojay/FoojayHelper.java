package io.foojay.socialposter.foojay;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoojayHelper {
    private static final Logger LOGGER = LogManager.getLogger(FoojayHelper.class);

    private static final String RSS_FEED_URL = "https://foojay.io/feed/";

    private FoojayHelper() {
        // Hide constructor
    }

    public static Optional<SyndFeed> getRSSFeed() {
        try (XmlReader reader = new XmlReader(new URL(RSS_FEED_URL))) {
            SyndFeedInput input = new SyndFeedInput();
            var feed = input.build(reader);
            LOGGER.info("Loaded RSS feed: {}, with {} messages", feed.getTitle(), feed.getEntries().size());
            return Optional.of(feed);
        } catch (IOException | FeedException e) {
            LOGGER.error("Failed to load RSS feed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public static List<Author> getAuthors(String url) {
        var authors = new ArrayList<Author>();

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            // Try to find multiple authors in multiple-authors__item-media
            Elements multipleAuthors = doc.select("div.multiple-authors__item");
            if (!multipleAuthors.isEmpty()) {
                authors.addAll(parseAuthorFromMultipleAuthorsBlock(multipleAuthors));
            }

            // Try to find author in author-block
            Elements authorBlock = doc.select("div.author-block");
            if (!authorBlock.isEmpty()) {
                var author = parseAuthorFromAuthorBlock(authorBlock.first());
                author.ifPresent(authors::add);
            }

            if (authors.isEmpty()) {
                LOGGER.warn("No author information found for URL: {}", url);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to fetch author profile from {}: {}", url, e.getMessage());
        }

        return authors;
    }

    private static List<Author> parseAuthorFromMultipleAuthorsBlock(Elements multipleAuthorsElement) {
        var authors = new ArrayList<Author>();
        try {
            for (Element authorElement : multipleAuthorsElement) {
                parseAuthorFromAuthorBlock(authorElement).ifPresent(authors::add);
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing author from multiple-authors block: {}", e.getMessage());
        }

        return authors;
    }

    private static Optional<Author> parseAuthorFromAuthorBlock(Element authorElement) {
        try {
            String name = "";
            String imageUrl = "";
            String linkedInUrl = "";
            String blueskyUrl = "";
            String mastodonUrl = "";
            String twitterUrl = "";
            String facebookUrl = "";

            // Extract author name
            Element nameElement = authorElement.selectFirst("h4");
            if (nameElement != null) {
                name = nameElement.text().trim();
            }

            // Extract author image
            Element imgElement = authorElement.selectFirst("img");
            if (imgElement != null) {
                imageUrl = imgElement.attr("src");
            }

            // Extract social profiles
            Elements socialLinks = authorElement.select("a");
            for (Element link : socialLinks) {
                String href = link.attr("href");
                if (href.contains("twitter.com") || href.contains("x.com")) {
                    twitterUrl = href;
                } else if (href.contains("linkedin.com")) {
                    linkedInUrl = href;
                } else if (href.contains("bsky.app")) {
                    blueskyUrl = href;
                } else if (href.contains("facebook.com")) {
                    facebookUrl = href;
                } else if (href.contains("mastodon") || href.contains("social")) {
                    mastodonUrl = href;
                }
            }

            if (name != null) {
                return Optional.of(new Author(name, imageUrl, linkedInUrl, blueskyUrl, mastodonUrl, twitterUrl, facebookUrl));
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing author from author-block: {}", e.getMessage());
        }

        return Optional.empty();
    }
}
