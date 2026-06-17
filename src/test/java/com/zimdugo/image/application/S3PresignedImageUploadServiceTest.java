package com.zimdugo.image.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.zimdugo.core.exception.BusinessException;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
class S3PresignedImageUploadServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

    @Test
    void createPresignedUploadSignsContentLength() throws Exception {
        given(presignedPutObjectRequest.url()).willReturn(URI.create("https://s3.example.com/upload").toURL());
        given(s3Presigner.presignPutObject(org.mockito.ArgumentMatchers.any(PutObjectPresignRequest.class)))
            .willReturn(presignedPutObjectRequest);
        S3PresignedImageUploadService service = new S3PresignedImageUploadService(
            s3Presigner,
            properties()
        );

        PresignedUploadResult result = service.createPresignedUpload(
            UploadCategory.LOCKER_REPORT,
            "locker.jpg",
            "image/jpeg",
            1024L,
            1L
        );

        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
        assertThat(captor.getValue().putObjectRequest().contentLength()).isEqualTo(1024L);
        assertThat(result.fileUrl()).startsWith("https://cdn.example.com/reports/");
    }

    @Test
    void createPresignedUploadWithTooLargeContentLengthFails() {
        S3PresignedImageUploadService service = new S3PresignedImageUploadService(
            s3Presigner,
            properties()
        );

        assertThatThrownBy(() -> service.createPresignedUpload(
            UploadCategory.LOCKER_REPORT,
            "locker.jpg",
            "image/jpeg",
            10_485_761L,
            1L
        )).isInstanceOf(BusinessException.class);
    }

    private S3ImageProperties properties() {
        return new S3ImageProperties(
            "ap-northeast-2",
            "test-bucket",
            "https://cdn.example.com",
            10,
            10_485_760
        );
    }
}
