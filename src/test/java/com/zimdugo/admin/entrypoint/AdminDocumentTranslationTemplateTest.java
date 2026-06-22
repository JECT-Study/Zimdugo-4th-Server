package com.zimdugo.admin.entrypoint;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class AdminDocumentTranslationTemplateTest {

    @Test
    @DisplayName("전체 초안이 있으면 선택한 언어만 다시 번역할 수 있다")
    void regeneratesOnlyRequestedLanguageDraft() throws IOException {
        String content = template();

        assertThat(content).contains(
            "regenerate-language-draft",
            "data-redraft-url",
            "th:if=\"${draft != null}\"",
            "draft-error",
            "meta[name=\"_csrf\"]",
            "response.data",
            "updateDraftRow",
            "data-section-id",
            "translations/draft/{language}",
            "다시 번역"
        );
    }

    @Test
    @DisplayName("링크 이동을 승인하면 브라우저 기본 이탈 경고를 비활성화한다")
    void marksConfirmedNavigationAsSafe() throws IOException {
        String content = template();

        assertThat(content).containsIgnoringWhitespaces("""
            if (!confirmed) {
                event.preventDefault();
                return;
            }
            safeSubmit = true;
            """);
    }

    private String template() throws IOException {
        return new ClassPathResource("templates/admin/document-translations.html")
            .getContentAsString(StandardCharsets.UTF_8);
    }
}
