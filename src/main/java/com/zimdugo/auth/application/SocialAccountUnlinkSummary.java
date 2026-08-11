package com.zimdugo.auth.application;

import java.util.List;

public record SocialAccountUnlinkSummary(
    int unlinkedCount,
    int skippedUnsupportedProviderCount,
    int skippedMissingTokenCount,
    int failedExternalCount
) {
    public static SocialAccountUnlinkSummary from(List<SocialAccountUnlinkResult> results) {
        int unlinkedCount = 0;
        int skippedUnsupportedProviderCount = 0;
        int skippedMissingTokenCount = 0;
        int failedExternalCount = 0;

        for (SocialAccountUnlinkResult result : results) {
            switch (result.status()) {
                case UNLINKED -> unlinkedCount++;
                case SKIPPED_UNSUPPORTED_PROVIDER -> skippedUnsupportedProviderCount++;
                case SKIPPED_TOKEN_MISSING -> skippedMissingTokenCount++;
                case FAILED_EXTERNAL -> failedExternalCount++;
                default -> throw new IllegalStateException("지원하지 않는 연동 해제 상태입니다: " + result.status());
            }
        }

        return new SocialAccountUnlinkSummary(
            unlinkedCount,
            skippedUnsupportedProviderCount,
            skippedMissingTokenCount,
            failedExternalCount
        );
    }
}
