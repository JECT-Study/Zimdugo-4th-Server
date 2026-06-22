package com.zimdugo.admin.translation;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationSource;
import com.zimdugo.common.i18n.SupportedLanguage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class GeminiDocumentTranslationDraftGeneratorTest {

    @Test
    void createsPromptForRequestedLanguageOnly() throws Exception {
        GeminiDocumentTranslationDraftGenerator generator = generator();

        var request = generator.request(source(), List.of(SupportedLanguage.JAPANESE));
        String prompt = request.contents().getFirst().parts().getFirst().text();

        assertThat(prompt).contains("Requested language tags: ja");
        assertThat(prompt).doesNotContain(
            "Requested language tags: ko",
            "Requested language tags: en",
            "Requested language tags: zh-Hans",
            "Requested language tags: zh-Hant"
        );
    }

    private GeminiDocumentTranslationDraftGenerator generator() {
        return new GeminiDocumentTranslationDraftGenerator(
            RestClient.create("https://example.com"),
            new GeminiTranslationProperties(
                "https://example.com",
                "api-key",
                "gemini-test",
                1000,
                1000
            ),
            new ObjectMapper()
        );
    }

    private AdminDocumentTranslationSource source() {
        return new AdminDocumentTranslationSource(
            1L,
            "NOTICE",
            "서비스 점검 안내",
            List.of(new AdminDocumentTranslationSource.Section(
                10L,
                "점검",
                "서비스 이용이 일시 중단됩니다."
            ))
        );
    }
}
