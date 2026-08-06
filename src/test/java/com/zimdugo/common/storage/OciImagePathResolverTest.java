package com.zimdugo.common.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zimdugo.core.exception.BusinessException;
import org.junit.jupiter.api.Test;

class OciImagePathResolverTest {

    private static final String BASE_URL =
        "https://objectstorage.ap-osaka-1.oraclecloud.com/n/testnamespace/b/test-bucket/o";

    @Test
    void encodeObjectNameAsOnePathParameterAndResolveItBack() {
        OciImagePathResolver resolver = new OciImagePathResolver(properties());

        String url = resolver.buildPublicUrl("reports/photo name.jpg");

        assertThat(url).isEqualTo(BASE_URL + "/reports%2Fphoto%20name.jpg");
        assertThat(resolver.resolveReportImageKey(url)).isEqualTo("reports/photo name.jpg");
    }

    @Test
    void rejectLookalikeHostAndNonReportKey() {
        OciImagePathResolver resolver = new OciImagePathResolver(properties());

        assertThatThrownBy(() -> resolver.resolveKey(
            "https://objectstorage.ap-osaka-1.oraclecloud.com.evil.test/"
                + "n/testnamespace/b/test-bucket/o/reports%2Fx.jpg"
        )).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> resolver.resolveReportImageKey(
            resolver.buildPublicUrl("profiles/1/x.jpg")
        )).isInstanceOf(BusinessException.class);
    }

    private OciObjectStorageProperties properties() {
        return new OciObjectStorageProperties(
            "ap-osaka-1",
            "testnamespace",
            "test-bucket",
            OciAuthenticationMode.INSTANCE_PRINCIPAL,
            " DEFAULT ",
            10,
            10_485_760
        );
    }
}
