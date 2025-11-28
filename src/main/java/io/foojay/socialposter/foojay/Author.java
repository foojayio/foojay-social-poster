package io.foojay.socialposter.foojay;

import io.foojay.socialposter.social.SocialPlatform;

public record Author(String name, String picture,
                     String linkedIn, String bluesky, String mastodon, String twitter, String facebook) {

    public String getSocialLink(SocialPlatform platform) {
        return switch (platform) {
            case BLUESKY -> bluesky == null ? "" : bluesky.replace("https://bsky.app/profile/", "@");
            case LINKEDIN -> linkedIn;
            case MASTODON -> mastodon;
            case FACEBOOK -> facebook;
            case TWITTER -> twitter;
        };
    }
}
