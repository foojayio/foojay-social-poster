package io.foojay.socialposter.llm;

import com.rometools.rome.feed.synd.SyndEntry;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.foojay.socialposter.foojay.Author;
import io.foojay.socialposter.social.SocialPlatform;
import io.foojay.socialposter.tool.AlertHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.ResourceBundle;

public class LlmHelper {
    private static final Logger LOGGER = LogManager.getLogger(LlmHelper.class);

    private ChatModel chatModel;
    private boolean initialized = false;

    public LlmHelper() {
        try {
            var key = ResourceBundle.getBundle("application").getString("OPENAI_API_KEY");
            if (key.isEmpty()) {
                AlertHelper.showAlert("API Key not set", "Please configure the OPENAI_API_KEY.");
            } else {
                chatModel = OpenAiChatModel.builder()
                        .apiKey(key)
                        .modelName("gpt-4")
                        .build();
                initialized = true;
            }
        } catch (Exception e) {
            AlertHelper.showAlert("API Key not set", "Please configure the OPENAI_API_KEY.");
            LOGGER.error("Application properties are not available: {}", e.getMessage());
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String generateMessage(SocialPlatform platform, SyndEntry selectedPost, List<Author> authors) {
        StringBuilder authorPrompt = new StringBuilder();

        if (authors != null && !authors.isEmpty()) {
            authorPrompt.append("Use the following author information with the correct links (if available):\n")
                    .append("   Number of authors: ").append(authors.size()).append("\n")
                    .append("   Don't modify the links, use them as provided").append("\n");

            for (var author : authors) {
                authorPrompt.append(" Author: ").append(author.name()).append("\n")
                        .append("    Link: ").append(author.getSocialLink(platform)).append("\n");
            }
        }

        var prompt = String.format(
                """
                        Create a short %s post (max %s characters) for this article:
                        Title: %s
                        Link: %s
                        
                        %s
                        
                        Write the message as being the publisher and maintainer of the Foojay website.
                        Avoid typical LLM words and characters.
                        make it engaging and include relevant hashtags. Keep it concise.
                        Make sure to include a link to the original article.""",
                platform.getLabel(),
                platform.getMaxLength(),
                selectedPost.getTitle(),
                selectedPost.getLink(),
                authorPrompt
        );

        return chatModel.chat(prompt);
    }
}
