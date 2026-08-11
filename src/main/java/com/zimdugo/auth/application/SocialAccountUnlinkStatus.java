package com.zimdugo.auth.application;

public enum SocialAccountUnlinkStatus {
    UNLINKED,
    SKIPPED_UNSUPPORTED_PROVIDER,
    SKIPPED_TOKEN_MISSING,
    FAILED_EXTERNAL
}
