package com.zimdugo.admin.translation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.admin.translation.dto.LockerReportTranslationSource;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.ExternalApiException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
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

    @Test
    void translatesServiceUnavailableResponseToKoreanMessage() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://example.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(request -> { })
            .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"error":{"message":"This model is currently experiencing high demand."}}
                    """));
        GeminiLockerReportTranslationDraftGenerator generator = generator(builder.build());

        assertThatThrownBy(() -> generator.generate(source()))
            .isInstanceOf(ExternalApiException.class)
            .hasMessage("번역 모델에 요청이 몰려 일시적으로 이용할 수 없습니다. 잠시 후 다시 시도해 주세요.");
        server.verify();
    }

    private GeminiLockerReportTranslationDraftGenerator generator() {
        return generator(RestClient.create("https://example.com"));
    }

    private GeminiLockerReportTranslationDraftGenerator generator(RestClient restClient) {
        return new GeminiLockerReportTranslationDraftGenerator(
            restClient,
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
