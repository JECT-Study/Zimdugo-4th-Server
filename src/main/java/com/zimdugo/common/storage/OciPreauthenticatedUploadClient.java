package com.zimdugo.common.storage;

import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.exception.ExternalApiException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OciPreauthenticatedUploadClient {

    private final OciObjectStorageClientProvider clientProvider;
    private final OciObjectStorageProperties properties;
    private final OciImagePathResolver pathResolver;
    private final Clock clock;

    @Autowired
    public OciPreauthenticatedUploadClient(
        OciObjectStorageClientProvider clientProvider,
        OciObjectStorageProperties properties,
        OciImagePathResolver pathResolver
    ) {
        this(clientProvider, properties, pathResolver, Clock.systemUTC());
    }

    OciPreauthenticatedUploadClient(
        OciObjectStorageClientProvider clientProvider,
        OciObjectStorageProperties properties,
        OciImagePathResolver pathResolver,
        Clock clock
    ) {
        this.clientProvider = clientProvider;
        this.properties = properties;
        this.pathResolver = pathResolver;
        this.clock = clock;
    }

    public PreauthenticatedUpload createObjectWrite(String key) {
        Instant expiresAt = clock.instant()
            .plus(Duration.ofMinutes(properties.uploadExpirationMinutes()));
        CreatePreauthenticatedRequestDetails details = createDetails(key, expiresAt);
        CreatePreauthenticatedRequestResponse response = requestPreauthenticatedUpload(
            key,
            details
        );
        String accessUri = extractAccessUri(response);
        return new PreauthenticatedUpload(
            properties.objectStorageEndpoint() + accessUri,
            pathResolver.buildPublicUrl(key),
            key,
            expiresAt
        );
    }

    private CreatePreauthenticatedRequestDetails createDetails(String key, Instant expiresAt) {
        return CreatePreauthenticatedRequestDetails.builder()
            .name("upload-" + UUID.randomUUID())
            .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectWrite)
            .objectName(key)
            .timeExpires(Date.from(expiresAt))
            .build();
    }

    private CreatePreauthenticatedRequestResponse requestPreauthenticatedUpload(
        String key,
        CreatePreauthenticatedRequestDetails details
    ) {
        CreatePreauthenticatedRequestResponse response;
        try {
            response = clientProvider.get()
                .createPreauthenticatedRequest(CreatePreauthenticatedRequestRequest.builder()
                    .namespaceName(properties.namespace())
                    .bucketName(properties.bucket())
                    .createPreauthenticatedRequestDetails(details)
                    .build());
        } catch (BmcException exception) {
            log.error(
                "OCI Object Storage PAR 생성 실패. bucket={}, key={}",
                properties.bucket(),
                key
            );
            throw new ExternalApiException(ErrorCode.IMAGE_STORAGE_WRITE_FAILED, exception);
        }
        return response;
    }

    private String extractAccessUri(CreatePreauthenticatedRequestResponse response) {
        if (response == null || response.getPreauthenticatedRequest() == null) {
            throw new ExternalApiException(ErrorCode.IMAGE_STORAGE_WRITE_FAILED);
        }
        String accessUri = response.getPreauthenticatedRequest().getAccessUri();
        if (accessUri == null || accessUri.isBlank()) {
            throw new ExternalApiException(ErrorCode.IMAGE_STORAGE_WRITE_FAILED);
        }
        return accessUri;
    }
}
