package com.zimdugo.admin.entrypoint.dto;

import com.zimdugo.common.storage.PresignedUpload;
import java.time.Instant;

public record AdminPresignedUploadResponse(
    String uploadUrl,
    String fileUrl,
    String key,
    Instant expiresAt
) {

    public static AdminPresignedUploadResponse from(PresignedUpload upload) {
        return new AdminPresignedUploadResponse(
            upload.uploadUrl(),
            upload.fileUrl(),
            upload.key(),
            upload.expiresAt()
        );
    }
}
