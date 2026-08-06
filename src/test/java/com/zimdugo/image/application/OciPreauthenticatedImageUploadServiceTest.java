package com.zimdugo.image.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.zimdugo.common.storage.ImageUploadPolicy;
import com.zimdugo.common.storage.OciAuthenticationMode;
import com.zimdugo.common.storage.OciImagePathResolver;
import com.zimdugo.common.storage.OciObjectStorageProperties;
import com.zimdugo.common.storage.OciPreauthenticatedUploadClient;
import com.zimdugo.common.storage.PreauthenticatedUpload;
import com.zimdugo.core.exception.BusinessException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OciPreauthenticatedImageUploadServiceTest {

    @Mock
    private OciPreauthenticatedUploadClient uploadClient;

    @Test
    void issueParForGeneratedReportImageKey() {
        OciImagePathResolver pathResolver = pathResolver();
        given(uploadClient.createObjectWrite(anyString()))
            .willAnswer(invocation -> new PreauthenticatedUpload(
                "https://objectstorage.ap-osaka-1.oraclecloud.com/p/token",
                pathResolver.buildPublicUrl(invocation.getArgument(0)),
                invocation.getArgument(0),
                Instant.parse("2026-08-03T00:10:00Z")
            ));
        OciPreauthenticatedImageUploadService service = service();

        PresignedUploadResult result = service.createPresignedUpload(
            UploadCategory.LOCKER_REPORT,
            "locker.jpg",
            "image/jpeg; charset=binary",
            1024L,
            1L
        );

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(uploadClient).createObjectWrite(key.capture());
        assertThat(key.getValue()).startsWith("reports/");
        assertThat(result.uploadUrl()).isEqualTo("https://objectstorage.ap-osaka-1.oraclecloud.com/p/token");
        assertThat(result.fileUrl()).contains("/reports%2F");
        assertThat(result.key()).isEqualTo(key.getValue());
        assertThat(result.expiresAt()).isEqualTo(Instant.parse("2026-08-03T00:10:00Z"));
    }

    @Test
    void rejectDeclaredSizeAboveConfiguredMaximumBeforeCallingOci() {
        assertThatThrownBy(() -> service().createPresignedUpload(
            UploadCategory.LOCKER_REPORT,
            "locker.jpg",
            "image/jpeg",
            10_485_761L,
            1L
        )).isInstanceOf(BusinessException.class);

        verifyNoInteractions(uploadClient);
    }

    private OciPreauthenticatedImageUploadService service() {
        return new OciPreauthenticatedImageUploadService(
            properties(),
            new ImageUploadPolicy(),
            pathResolver(),
            uploadClient
        );
    }

    private OciImagePathResolver pathResolver() {
        return new OciImagePathResolver(properties());
    }

    private OciObjectStorageProperties properties() {
        return new OciObjectStorageProperties(
            "ap-osaka-1",
            "test-namespace",
            "test-bucket",
            OciAuthenticationMode.CONFIG_FILE,
            "DEFAULT",
            10,
            10_485_760
        );
    }
}
