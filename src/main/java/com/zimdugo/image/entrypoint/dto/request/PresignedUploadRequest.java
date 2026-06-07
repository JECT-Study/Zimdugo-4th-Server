package com.zimdugo.image.entrypoint.dto.request;

import com.zimdugo.image.application.UploadCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PresignedUploadRequest(
    @Schema(description = "업로드 카테고리", example = "LOCKER_REPORT")
    @NotNull
    UploadCategory category,

    @Schema(description = "원본 파일명", example = "locker-photo.jpg")
    @NotBlank
    @Size(max = 255)
    String fileName,

    @Schema(description = "파일 Content-Type", example = "image/jpeg")
    @NotBlank
    @Size(max = 100)
    String contentType
) {
}
