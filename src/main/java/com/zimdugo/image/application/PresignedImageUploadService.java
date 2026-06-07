package com.zimdugo.image.application;

public interface PresignedImageUploadService {

    PresignedUploadResult createPresignedUpload(
        UploadCategory category,
        String originalFileName,
        String contentType,
        Long userId
    );
}
