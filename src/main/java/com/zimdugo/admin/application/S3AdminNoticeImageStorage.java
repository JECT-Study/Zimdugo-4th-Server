package com.zimdugo.admin.application;

import com.zimdugo.common.storage.ImageUploadPolicy;
import com.zimdugo.common.storage.S3ImagePathResolver;
import com.zimdugo.common.storage.S3StorageProperties;
import com.zimdugo.core.exception.ExternalApiException;
import com.zimdugo.core.exception.ErrorCode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3AdminNoticeImageStorage implements AdminNoticeImageStorage {

    private static final String NOTICE_IMAGE_KEY_PREFIX = "admin/notice-images/";

    private final S3Client s3Client;
    private final S3StorageProperties properties;
    private final S3ImagePathResolver pathResolver;
    private final ImageUploadPolicy imageUploadPolicy;
    private final AdminNoticeImageFileValidator fileValidator;

    @Override
    public List<String> uploadAll(List<MultipartFile> files) {
        files.forEach(fileValidator::validate);
        List<String> uploadedUrls = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                uploadedUrls.add(upload(file));
            }
            return uploadedUrls;
        } catch (RuntimeException exception) {
            deleteAll(uploadedUrls);
            throw exception;
        }
    }

    @Override
    public void deleteAll(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            try {
                String key = pathResolver.resolveKey(imageUrl);
                s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(key)
                    .build());
            } catch (RuntimeException exception) {
                log.warn("공지 이미지 S3 삭제 실패. imageUrl: {}", imageUrl, exception);
            }
        }
    }

    private String upload(MultipartFile file) {
        String extension = imageUploadPolicy.extractValidExtension(file.getOriginalFilename());
        String contentType = imageUploadPolicy.validateContentType(file.getContentType());
        String key = pathResolver.createImageKey(NOTICE_IMAGE_KEY_PREFIX, extension);
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(properties.bucket())
            .key(key)
            .contentType(contentType)
            .contentLength(file.getSize())
            .build();
        try {
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return pathResolver.buildPublicUrl(key);
        } catch (IOException | SdkException exception) {
            throw new ExternalApiException(ErrorCode.IMAGE_STORAGE_WRITE_FAILED, exception);
        }
    }
}
