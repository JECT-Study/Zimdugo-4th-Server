package com.zimdugo.admin.entrypoint;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class AdminDocumentTranslationTemplateTest {

    @Test
    @DisplayName("링크 이동을 승인하면 브라우저 기본 이탈 경고를 비활성화한다")
    void marksConfirmedNavigationAsSafe() throws IOException {
        ClassPathResource template = new ClassPathResource(
            "templates/admin/document-translations.html"
        );
        String content = template.getContentAsString(StandardCharsets.UTF_8);

        assertThat(content).containsIgnoringWhitespaces("""
            if (!confirmed) {
                event.preventDefault();
                return;
            }
            safeSubmit = true;
            """);
    }
}
