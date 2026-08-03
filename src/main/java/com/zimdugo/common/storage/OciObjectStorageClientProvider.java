package com.zimdugo.common.storage;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OciObjectStorageClientProvider implements AutoCloseable {

    private final OciObjectStorageProperties properties;
    private volatile ObjectStorage client;

    public ObjectStorage get() {
        ObjectStorage current = client;
        if (current == null) {
            synchronized (this) {
                current = client;
                if (current == null) {
                    current = createClient();
                    client = current;
                }
            }
        }
        return current;
    }

    private ObjectStorage createClient() {
        AbstractAuthenticationDetailsProvider provider = switch (properties.authMode()) {
            case INSTANCE_PRINCIPAL ->
                InstancePrincipalsAuthenticationDetailsProvider.builder().build();
            case CONFIG_FILE -> configFileProvider();
        };
        return ObjectStorageClient.builder()
            .region(Region.fromRegionId(properties.region()))
            .build(provider);
    }

    private AbstractAuthenticationDetailsProvider configFileProvider() {
        try {
            return new ConfigFileAuthenticationDetailsProvider(
                properties.normalizedConfigProfile()
            );
        } catch (IOException exception) {
            throw new IllegalStateException("OCI config profile을 읽을 수 없습니다.", exception);
        }
    }

    @PreDestroy
    @Override
    public void close() {
        ObjectStorage current = client;
        if (current != null) {
            try {
                current.close();
            } catch (Exception exception) {
                throw new IllegalStateException(
                    "OCI Object Storage client를 종료할 수 없습니다.",
                    exception
                );
            }
        }
    }
}
