package io.foojay.socialposter;

import com.rometools.rome.feed.synd.SyndEntry;
import io.foojay.socialposter.foojay.FoojayHelper;
import io.foojay.socialposter.llm.LlmHelper;
import io.foojay.socialposter.social.SocialPlatform;
import io.foojay.socialposter.tool.AlertHelper;
import io.foojay.socialposter.tool.ClipboardHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class SocialPoster extends Application {
    private static final Logger LOGGER = LogManager.getLogger(SocialPoster.class);

    private final LlmHelper llmHelper = new LlmHelper();

    private final TextArea blueskyTextArea = new TextArea();
    private final TextArea mastodonTextArea = new TextArea();
    private final TextArea linkedinTextArea = new TextArea();
    private final WebView webView = new WebView();
    private ComboBox<SyndEntry> postSelector;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Foojay Social Media Manager");

        // Main layout
        var root = new BorderPane();
        root.setPadding(new Insets(15));

        root.setTop(createTopSection());

        var splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        splitPane.getItems().addAll(createCenterSection(), webView);
        splitPane.setDividerPositions(0.3);
        root.setCenter(splitPane);

        statusLabel = new Label("Loading RSS feed...");
        statusLabel.setStyle("-fx-font-style: italic;");
        root.setBottom(statusLabel);

        primaryStage.setScene(new Scene(root, 1400, 1000));
        primaryStage.show();

        // Check LLM
        if (!llmHelper.isInitialized()) {
            statusLabel.setText("Problem with the LLM, will not be able to generate messages");
        }

        // Load RSS feed in background and show the results
        showRSS();
    }

    private VBox createTopSection() {
        var topBox = new VBox(10);
        topBox.setPadding(new Insets(0, 0, 15, 0));

        var titleLabel = new Label("Select a recent post:");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        postSelector = new ComboBox<>();
        postSelector.setMaxWidth(Double.MAX_VALUE);
        postSelector.setPromptText("Select a post...");
        postSelector.setOnAction(e -> generateMessages());
        postSelector.setCellFactory(p -> new ListCell<>() {
            @Override
            protected void updateItem(SyndEntry item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(item.getPublishedDate()) + ": " + item.getTitle());
            }
        });
        postSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SyndEntry item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getTitle());
            }
        });

        var refreshButton = new Button("Refresh Feed");
        refreshButton.setOnAction(_ -> showRSS());

        var buttonBox = new HBox(10, refreshButton);

        topBox.getChildren().addAll(titleLabel, postSelector, buttonBox);
        return topBox;
    }

    private VBox createCenterSection() {
        var centerBox = new VBox(15);
        centerBox.setPadding(new Insets(10, 0, 10, 0));
        centerBox.getChildren().add(createPlatformSection(SocialPlatform.BLUESKY, blueskyTextArea));
        centerBox.getChildren().add(createPlatformSection(SocialPlatform.MASTODON, mastodonTextArea));
        centerBox.getChildren().add(createPlatformSection(SocialPlatform.LINKEDIN, linkedinTextArea));
        return centerBox;
    }

    private VBox createPlatformSection(SocialPlatform platform, TextArea textArea) {
        var section = new VBox(5);
        section.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 10; -fx-background-color: #f9f9f9;");

        var platformLabel = new Label(platform.name());
        platformLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        var charCountLabel = new Label("Characters: 0");
        charCountLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");

        var contentBox = new VBox(10);

        var publishButton = new Button("Copy to clipboard"); // ("Publish to " + platform.name());
        publishButton.setOnAction(_ -> publishMessage(platform, textArea.getText()));
        publishButton.setMaxWidth(Double.MAX_VALUE);

        textArea.setPrefRowCount(10);
        textArea.setWrapText(true);
        textArea.setPromptText("Generated message will appear here...");
        textArea.textProperty().addListener((obs, old, text) -> {
            int charCount = (text != null) ? text.length() : 0;
            publishButton.setDisable(charCount == 0);
            charCountLabel.setText("Characters: " + charCount + "/" + platform.getMaxLength());
        });

        contentBox.getChildren().addAll(textArea, charCountLabel, publishButton);

        section.getChildren().addAll(platformLabel, contentBox);
        return section;
    }

    private void showRSS() {
        CompletableFuture.runAsync(() -> {
            try {
                Platform.runLater(() -> statusLabel.setText("Loading RSS feed..."));

                var feed = FoojayHelper.getRSSFeed();

                if (feed.isEmpty()) {
                    AlertHelper.showAlert("RSS Feed Error", "Failed to load RSS feed.");
                    return;
                }

                var today = LocalDate.now().minusDays(5);
                var todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
                postSelector.getItems().clear();

                var recentPosts = feed.get().getEntries().stream()
                        .filter(entry -> entry.getPublishedDate() != null && entry.getPublishedDate().after(todayDate))
                        .toList();

                Platform.runLater(() -> {
                    postSelector.getItems().clear();
                    postSelector.getItems().addAll(recentPosts);
                    if (recentPosts.isEmpty()) {
                        statusLabel.setText("No posts published recently.");
                    } else {
                        statusLabel.setText(String.format("Found %d recently published post(s).", recentPosts.size()));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error loading RSS feed: " + e.getMessage());
                    AlertHelper.showAlert("Error", "Failed to load RSS feed: " + e.getMessage());
                });

                LOGGER.error("Failed to show RSS feed: {}", e.getMessage());
            }
        });
    }

    private void generateMessages() {
        var selectedPost = postSelector.getSelectionModel().getSelectedItem();

        if (selectedPost == null) {
            return;
        }

        if (selectedPost.getLink() != null) {
            webView.getEngine().load(selectedPost.getLink());
        }

        if (!llmHelper.isInitialized()) {
            AlertHelper.showAlert("LLM is not ready", "Can't generate messages!");
            return;
        }

        blueskyTextArea.setText("");
        mastodonTextArea.setText("");
        linkedinTextArea.setText("");

        CompletableFuture.runAsync(() -> {
            try {
                Platform.runLater(() -> statusLabel.setText("Generating messages..."));

                var authors = FoojayHelper.getAuthors(selectedPost.getLink());

                // Generate Bluesky message
                var blueskyMessage = llmHelper.generateMessage(SocialPlatform.BLUESKY, selectedPost, authors);
                Platform.runLater(() -> blueskyTextArea.setText(blueskyMessage));

                // Generate Mastodon message
                var mastodonMessage = llmHelper.generateMessage(SocialPlatform.MASTODON, selectedPost, authors);
                Platform.runLater(() -> mastodonTextArea.setText(mastodonMessage));

                // Generate LinkedIn message
                var linkedinMessage = llmHelper.generateMessage(SocialPlatform.LINKEDIN, selectedPost, authors);
                Platform.runLater(() -> linkedinTextArea.setText(linkedinMessage));

                Platform.runLater(() -> statusLabel.setText("Messages generated successfully!"));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error generating messages: " + e.getMessage());
                    AlertHelper.showAlert("Error", "Failed to generate messages: " + e.getMessage());
                });

                LOGGER.error("Failed to generate messages: {}", e.getMessage());
            }
        });
    }

    private void publishMessage(SocialPlatform platform, String message) {
        ClipboardHelper.copyToClipboard(message);
        statusLabel.setText("Message for " + platform.getLabel() + " copied to clipboard!");
        /*
        switch (platform) {
            case BLUESKY -> BlueSkyHelper.sendMessage(message, statusLabel);
            case MASTODON -> MastodonHelper.sendMessage(message, statusLabel);
            case LINKEDIN -> LinkedInHelper.sendMessage(message, statusLabel);
            default -> LOGGER.warn("No publishing available for: {}", platform);
        }
        */
    }
}
