package com.zimdugo.admin.entrypoint;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class AdminDocumentListTemplateTest {

    @Test
    @DisplayName("번역 누락으로 문서 적용에 실패하면 알림 후 번역 확인 화면으로 이동한다")
    void redirectsMissingTranslationToReviewPage() throws IOException {
        ClassPathResource template = new ClassPathResource("templates/admin/list.html");
        String content = template.getContentAsString(StandardCharsets.UTF_8);

        assertThat(content).containsIgnoringWhitespaces("""
            alert(error.message);
            if (error.code === 'ADMIN-400-3') {
                window.location.href = '/admin/documents/' + id + '/translations';
                return;
            }
            location.reload();
            """);
    }
}
