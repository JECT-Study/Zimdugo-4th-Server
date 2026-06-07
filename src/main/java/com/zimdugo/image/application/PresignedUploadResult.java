package com.zimdugo.image.application;

import java.time.Instant;

public record PresignedUploadResult(
    String uploadUrl,
    String fileUrl,
    String key,
    Instant expiresAt
) {
}
