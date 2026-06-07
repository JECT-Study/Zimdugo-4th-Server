package com.zimdugo.image.entrypoint.dto.response;

import com.zimdugo.image.application.PresignedUploadResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record PresignedUploadResponse(
    @Schema(description = "S3 PUT 업로드용 presigned URL")
    String uploadUrl,

    @Schema(description = "업로드 후 저장해서 사용할 공개 URL")
    String fileUrl,

    @Schema(description = "저장된 객체 key")
    String key,

    @Schema(description = "presigned URL 만료 시각")
    Instant expiresAt
) {
    public static PresignedUploadResponse from(PresignedUploadResult result) {
        return new PresignedUploadResponse(
            result.uploadUrl(),
            result.fileUrl(),
            result.key(),
            result.expiresAt()
        );
    }
}
