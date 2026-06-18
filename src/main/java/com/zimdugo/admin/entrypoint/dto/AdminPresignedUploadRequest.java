package com.zimdugo.admin.entrypoint.dto;

import com.zimdugo.admin.application.AdminUploadCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AdminPresignedUploadRequest(
    @NotNull
    AdminUploadCategory category,

    @NotBlank
    @Size(max = 255)
    String fileName,

    @NotBlank
    @Size(max = 100)
    String contentType,

    @NotNull
    @Positive
    Long contentLength
) {
}
