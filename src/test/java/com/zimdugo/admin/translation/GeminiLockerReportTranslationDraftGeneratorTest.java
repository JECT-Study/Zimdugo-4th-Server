package com.zimdugo.admin.translation;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.admin.translation.dto.LockerReportTranslationSource;
import com.zimdugo.common.i18n.SupportedLanguage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class GeminiLockerReportTranslationDraftGeneratorTest {

    @Test
    void createsPromptForRequestedLanguageOnly() throws Exception {
        GeminiLockerReportTranslationDraftGenerator generator = generator();

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

    private GeminiLockerReportTranslationDraftGenerator generator() {
        return new GeminiLockerReportTranslationDraftGenerator(
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

    private LockerReportTranslationSource source() {
        return new LockerReportTranslationSource(
            1L,
            "서울역",
            "서울 중구 한강대로",
            "서울역 보관함",
            "서울 중구 한강대로",
            "ABOVE_GROUND",
            1,
            "INDOOR",
            "SUBWAY_STATION",
            "SMALL",
            "PAID",
            1000,
            3000,
            "1번 출구 근처",
            "OPEN_24_HOURS",
            null,
            null
        );
    }
}
