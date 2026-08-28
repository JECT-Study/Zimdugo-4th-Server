package com.zimdugo.admin.translation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationSource;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.ExternalApiException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
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

    @Test
    void translatesRateLimitResponseToKoreanMessage() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://example.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(request -> { })
            .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                    {"error":{"message":"You exceeded your current quota."}}
                    """));
        GeminiDocumentTranslationDraftGenerator generator = generator(builder.build());

        assertThatThrownBy(() -> generator.generate(source()))
            .isInstanceOf(ExternalApiException.class)
            .hasMessage("번역 API 사용 한도를 초과했습니다. 사용량과 요금제를 확인한 후 다시 시도해 주세요.");
        server.verify();
    }

    @Test
    void logsUnderlyingCauseWhenResponseBodyCannotBeRead() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://example.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(request -> { })
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.TEXT_PLAIN)
                .body("not-json"));
        GeminiDocumentTranslationDraftGenerator generator = generator(builder.build());
        Logger logger = (Logger) LoggerFactory.getLogger(GeminiDocumentTranslationDraftGenerator.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            assertThatThrownBy(() -> generator.generate(source()))
                .isInstanceOf(ExternalApiException.class);

            assertThat(appender.list)
                .anySatisfy(event -> {
                    assertThat(event.getFormattedMessage()).contains("문서 번역 초안 생성 실패");
                    assertThat(event.getThrowableProxy()).isNotNull();
                });
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
        server.verify();
    }

    private GeminiDocumentTranslationDraftGenerator generator() {
        return generator(RestClient.create("https://example.com"));
    }

    private GeminiDocumentTranslationDraftGenerator generator(RestClient restClient) {
        return new GeminiDocumentTranslationDraftGenerator(
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
