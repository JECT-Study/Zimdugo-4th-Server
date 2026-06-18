package com.zimdugo.common.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
class S3PresignedUploadClientTest {

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

    @Test
    void createPresignedPutObjectSignsContentLength() throws Exception {
        S3StorageProperties properties = properties();
        given(presignedPutObjectRequest.url()).willReturn(URI.create("https://s3.example.com/upload").toURL());
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
            .willReturn(presignedPutObjectRequest);
        S3PresignedUploadClient client = new S3PresignedUploadClient(
            s3Presigner,
            properties,
            new S3ImagePathResolver(properties)
        );

        PresignedUpload upload = client.createPresignedPutObject("reports/test.jpg", "image/jpeg", 1024L);

        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
        assertThat(captor.getValue().putObjectRequest().contentLength()).isEqualTo(1024L);
        assertThat(captor.getValue().putObjectRequest().contentType()).isEqualTo("image/jpeg");
        assertThat(upload.fileUrl()).isEqualTo("https://cdn.example.com/reports/test.jpg");
    }

    private S3StorageProperties properties() {
        return new S3StorageProperties(
            "ap-northeast-2",
            "test-bucket",
            "https://cdn.example.com",
            10,
            10_485_760
        );
    }
}
