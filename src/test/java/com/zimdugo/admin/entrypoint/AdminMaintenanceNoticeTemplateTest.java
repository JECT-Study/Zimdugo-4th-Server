package com.zimdugo.admin.entrypoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class AdminMaintenanceNoticeTemplateTest {

    @Test
    @DisplayName("점검 상태 토글은 스위치 전체에서 클릭할 수 있다")
    void usesFullSwitchAsToggleHitTarget() throws IOException {
        String template = new ClassPathResource("templates/admin/maintenance-notice-form.html")
            .getContentAsString(StandardCharsets.UTF_8);

        assertThat(template).contains("<label id=\"maintenanceStatus\" class=\"maintenance-status\"");
        assertThat(template).contains(".switch-input { position: absolute; inset: 0;");
        assertThat(template).contains(".switch-input:checked ~ .switch-track");
        assertThat(template).doesNotContain("clip: rect");
    }
}
