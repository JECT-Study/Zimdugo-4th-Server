package com.zimdugo.common.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NginxUploadConfigurationTest {

    @Test
    void deploymentRaisesNginxRequestLimitToMatchMultipartRequests() throws IOException {
        Path nginxConfigPath = Path.of("docker/nginx/zimdugo-upload-size.conf");
        String workflow = Files.readString(
            Path.of(".github/workflows/ci-cd.yml"),
            StandardCharsets.UTF_8
        );

        assertThat(nginxConfigPath).exists();
        assertThat(Files.readString(nginxConfigPath, StandardCharsets.UTF_8))
            .contains("client_max_body_size 105m;");
        assertThat(workflow)
            .contains("docker/nginx/zimdugo-upload-size.conf")
            .contains("/etc/nginx/conf.d/zimdugo-upload-size.conf")
            .contains("nginx -t")
            .contains("systemctl reload nginx");
    }
}
