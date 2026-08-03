package com.zimdugo.admin.application;

import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.zimdugo.common.storage.ImageUploadPolicy;
import com.zimdugo.common.storage.OciImagePathResolver;
import com.zimdugo.common.storage.OciObjectStorageClientProvider;
import com.zimdugo.common.storage.OciObjectStorageProperties;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.exception.ExternalApiException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class OciAdminLockerImageStorage implements AdminLockerImageStorage {

    private static final String LOCKER_IMAGE_KEY_PREFIX = "admin/notice-images/";

    private final OciObjectStorageClientProvider clientProvider;
    private final OciObjectStorageProperties properties;
    private final OciImagePathResolver pathResolver;
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
            String key;
            try {
                key = pathResolver.resolveKey(imageUrl);
            } catch (RuntimeException exception) {
                log.warn(
                    "보관함 이미지 OCI 삭제 실패. reason={}",
                    exception.getClass().getSimpleName()
                );
                continue;
            }
            if (!key.startsWith(LOCKER_IMAGE_KEY_PREFIX)) {
                log.warn("보관함 이미지 OCI 삭제 실패. reason=unexpected object key prefix");
                continue;
            }
            delete(key);
        }
    }

    private void delete(String key) {
        try {
            clientProvider.get().deleteObject(DeleteObjectRequest.builder()
                .namespaceName(properties.namespace())
                .bucketName(properties.bucket())
                .objectName(key)
                .build());
        } catch (RuntimeException exception) {
            log.warn(
                "보관함 이미지 OCI 삭제 실패. bucket={}, reason={}",
                properties.bucket(),
                exception.getClass().getSimpleName()
            );
        }
    }

    private String upload(MultipartFile file) {
        String extension = imageUploadPolicy.extractValidExtension(file.getOriginalFilename());
        String contentType = imageUploadPolicy.validateContentType(file.getContentType());
        String key = pathResolver.createImageKey(LOCKER_IMAGE_KEY_PREFIX, extension);
        try (InputStream inputStream = file.getInputStream()) {
            objectStorage().putObject(PutObjectRequest.builder()
                .namespaceName(properties.namespace())
                .bucketName(properties.bucket())
                .objectName(key)
                .contentType(contentType)
                .contentLength(file.getSize())
                .putObjectBody(inputStream)
                .build());
            return pathResolver.buildPublicUrl(key);
        } catch (IOException | BmcException exception) {
            log.error(
                "보관함 이미지 OCI 업로드 실패. bucket={}, key={}, contentType={}, fileSize={}, reason={}",
                properties.bucket(),
                key,
                contentType,
                file.getSize(),
                exception.getClass().getSimpleName()
            );
            throw new ExternalApiException(ErrorCode.IMAGE_STORAGE_WRITE_FAILED);
        }
    }

    private ObjectStorage objectStorage() {
        try {
            return clientProvider.get();
        } catch (RuntimeException exception) {
            log.error(
                "보관함 이미지 OCI 클라이언트 초기화 실패. reason={}",
                exception.getClass().getSimpleName()
            );
            throw new ExternalApiException(ErrorCode.IMAGE_STORAGE_WRITE_FAILED);
        }
    }
}
