package com.zimdugo.auth.application;

import com.zimdugo.user.domain.AuthProvider;

public record SocialAccountUnlinkResult(
    AuthProvider provider,
    SocialAccountUnlinkStatus status
) {
    public static SocialAccountUnlinkResult unlinked(AuthProvider provider) {
        return new SocialAccountUnlinkResult(provider, SocialAccountUnlinkStatus.UNLINKED);
    }

    public static SocialAccountUnlinkResult skippedUnsupportedProvider(AuthProvider provider) {
        return new SocialAccountUnlinkResult(provider, SocialAccountUnlinkStatus.SKIPPED_UNSUPPORTED_PROVIDER);
    }

    public static SocialAccountUnlinkResult skippedMissingToken(AuthProvider provider) {
        return new SocialAccountUnlinkResult(provider, SocialAccountUnlinkStatus.SKIPPED_TOKEN_MISSING);
    }

    public static SocialAccountUnlinkResult failedExternal(AuthProvider provider) {
        return new SocialAccountUnlinkResult(provider, SocialAccountUnlinkStatus.FAILED_EXTERNAL);
    }
}
