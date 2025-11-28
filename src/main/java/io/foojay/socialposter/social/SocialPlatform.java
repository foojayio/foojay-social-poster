package io.foojay.socialposter.social;

public enum SocialPlatform {
    BLUESKY("Bluesky", 300),
    FACEBOOK("Facebook", 1000),
    LINKEDIN("LinkedIn", 3000),
    MASTODON("Mastodon", 500),
    TWITTER("Twitter", 280);

    private final String label;
    private final int maxLength;

    SocialPlatform(String label, int maxLength) {
        this.label = label;
        this.maxLength = maxLength;
    }

    public String getLabel() {
        return label;
    }

    public int getMaxLength() {
        return maxLength;
    }
}
