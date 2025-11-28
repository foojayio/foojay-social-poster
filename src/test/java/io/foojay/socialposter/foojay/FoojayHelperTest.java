package io.foojay.socialposter.foojay;

import io.foojay.socialposter.social.SocialPlatform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FoojayHelperTest {

    @Test
    void shouldParseSingleAuthorsPage() {
        var url = "https://foojay.io/today/jc-ai-newsletter-10/";
        var authors = FoojayHelper.getAuthors(url);

        assertAll(
                () -> assertEquals(1, authors.size(), "Expects 1 author"),
                () -> assertEquals("Miro Wengner", authors.getFirst().name(), "Name"),
                () -> assertEquals("https://foojay.io/wp-content/uploads/2021/11/5yATaVf7_400x400-80x80.jpeg", authors.getFirst().picture(), "Picture"),
                () -> assertEquals("https://twitter.com/miragemiko", authors.getFirst().twitter(), "Twitter"),
                () -> assertEquals("https://twitter.com/miragemiko", authors.getFirst().getSocialLink(SocialPlatform.TWITTER), "Twitter"),
                () -> assertEquals("https://www.linkedin.com/in/mwengner/", authors.getFirst().linkedIn(), "LinkedIn"),
                () -> assertEquals("https://www.linkedin.com/in/mwengner/", authors.getFirst().getSocialLink(SocialPlatform.LINKEDIN), "LinkedIn")
        );
    }


    @Test
    void shouldParseMultipleAuthorsPage() {
        var url = "https://foojay.io/today/foojay-podcast-76/";
        var authors = FoojayHelper.getAuthors(url);

        assertAll(
                () -> assertEquals(3, authors.size(), "Expects 3 author"),

                () -> assertEquals("Frank Delporte", authors.getFirst().name(), "Name 1"),
                () -> assertEquals("https://foojay.io/wp-content/uploads/2025/05/cropped-frank-delporte-scaled-1-80x80.jpg", authors.getFirst().picture(), "Picture 1"),
                () -> assertEquals("https://bsky.app/profile/frankdelporte.be", authors.getFirst().bluesky(), "Bluesky 1"),
                () -> assertEquals("@frankdelporte.be", authors.getFirst().getSocialLink(SocialPlatform.BLUESKY), "Bluesky 1"),
                () -> assertEquals("https://www.linkedin.com/in/frankdelporte/", authors.getFirst().linkedIn(), "LinkedIn 1"),
                () -> assertEquals("https://foojay.social/@FrankDelporte", authors.getFirst().mastodon(), "Mastodon 1"),

                () -> assertEquals("Geertjan Wielenga", authors.get(1).name(), "Name 2"),
                () -> assertEquals("https://foojay.io/wp-content/uploads/2023/04/cropped-geertjan-3-41-32-80x80.png", authors.get(1).picture(), "Picture 2"),
                () -> assertEquals("https://twitter.com/geertjanw", authors.get(1).twitter(), "Twitter 2"),
                () -> assertEquals("https://www.facebook.com/geertjanw", authors.get(1).facebook(), "Facebook 2"),
                () -> assertEquals("https://www.facebook.com/geertjanw", authors.get(1).getSocialLink(SocialPlatform.FACEBOOK), "Facebook 2"),
                () -> assertEquals("https://www.linkedin.com/in/geertjanwielenga/", authors.get(1).linkedIn(), "LinkedIn 2"),

                () -> assertEquals("Jonathan Vila", authors.get(2).name(), "Name 3"),
                () -> assertEquals("https://secure.gravatar.com/avatar/df7e2e0365b6a5776a600eb1560c0ecf1fdba503377246026229ff50ffe7fd51?s=80&d=mm&r=g", authors.get(2).picture(), "Picture 3"),
                () -> assertEquals("https://twitter.com/vilojona", authors.get(2).twitter(), "Twitter 3"),
                () -> assertEquals("https://www.linkedin.com/in/jonathanvila/", authors.get(2).linkedIn(), "LinkedIn 3")

        );
    }
}
