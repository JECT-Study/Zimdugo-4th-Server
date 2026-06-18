package com.zimdugo.common.storage;

import java.time.Instant;

public record PresignedUpload(
    String uploadUrl,
    String fileUrl,
    String key,
    Instant expiresAt
) {
}
