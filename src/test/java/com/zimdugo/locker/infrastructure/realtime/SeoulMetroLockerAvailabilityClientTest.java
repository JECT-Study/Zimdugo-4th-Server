package com.zimdugo.locker.infrastructure.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.realtime.LockerRealtimeAvailabilitySnapshot;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SeoulMetroLockerAvailabilityClientTest {

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder().baseUrl("https://openapi.seoul.go.kr:8088");
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void readsLockerAvailabilityFromCurrentSeoulOpenApiResponse() {
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/1/1000/"))
            .andRespond(withSuccess(responseJson(), MediaType.APPLICATION_JSON));

        List<LockerRealtimeAvailabilitySnapshot> lockers = client().fetchAll();

        assertThat(lockers).containsExactly(new LockerRealtimeAvailabilitySnapshot(
            "TL124_DETAIL", 12, 2, 0
        ));
        server.verify();
    }

    @Test
    void readsAvailabilityWhenProviderResponseContainsUnescapedLineBreak() {
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/1/1000/"))
            .andRespond(withSuccess("""
                {
                  "response": {
                    "header": {"resultCode": "00", "resultMsg": "NORMAL_CODE"},
                    "body": {
                      "items": {
                        "item": [{
                          "lckrDtlId": "TL124_DETAIL",
                          "lckrLoc": "지하 1층
                개찰구 앞",
                          "usePsbltySmttypeLckrCnt": 12,
                          "usePsbltyMdtypeLckrCnt": 2,
                          "usePsbltyLrtypeLckrCnt": 0
                        }]
                      },
                      "totalCount": 1
                    }
                  }
                }
                """, MediaType.APPLICATION_JSON));

        assertThat(client().fetchAll()).containsExactly(new LockerRealtimeAvailabilitySnapshot(
            "TL124_DETAIL", 12, 2, 0
        ));
        server.verify();
    }

    @Test
    void rejectsProviderErrorResponse() {
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/1/1000/"))
            .andRespond(withSuccess("""
                {
                  "response": {
                    "header": {"resultCode": "ERROR", "resultMsg": "INVALID_REQUEST"}
                  }
                }
                """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(client()::fetchAll)
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_API_ERROR));
        server.verify();
    }

    @Test
    void rejectsMalformedAvailabilityCount() {
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/1/1000/"))
            .andRespond(withSuccess("""
                {
                  "response": {
                    "header": {"resultCode": "00", "resultMsg": "NORMAL_CODE"},
                    "body": {
                      "items": {
                        "item": [{
                          "lckrDtlId": "TL124_DETAIL",
                          "usePsbltySmttypeLckrCnt": "unknown",
                          "usePsbltyMdtypeLckrCnt": 2,
                          "usePsbltyLrtypeLckrCnt": 0
                        }]
                      },
                      "totalCount": 1
                    }
                  }
                }
                """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(client()::fetchAll)
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_API_ERROR));
        server.verify();
    }

    @Test
    void readsEveryPageReportedByTheProvider() {
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/1/1/"))
            .andRespond(withSuccess(pageResponseJson("TL1", 1, 2, 3, 2), MediaType.APPLICATION_JSON));
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/2/2/"))
            .andRespond(withSuccess(pageResponseJson("TL2", 4, 5, 6, 2), MediaType.APPLICATION_JSON));

        assertThat(client(1).fetchAll()).containsExactly(
            new LockerRealtimeAvailabilitySnapshot("TL1", 1, 2, 3),
            new LockerRealtimeAvailabilitySnapshot("TL2", 4, 5, 6)
        );
        server.verify();
    }

    @Test
    void rejectsIncompletePage() {
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/1/2/"))
            .andRespond(withSuccess(pageResponseJson("TL1", 1, 2, 3, 3), MediaType.APPLICATION_JSON));

        assertExternalApiError(client(2));
        server.verify();
    }

    @Test
    void rejectsDuplicateLockerIdAcrossPages() {
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/1/1/"))
            .andRespond(withSuccess(pageResponseJson("TL1", 1, 2, 3, 2), MediaType.APPLICATION_JSON));
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/2/2/"))
            .andRespond(withSuccess(pageResponseJson("TL1", 4, 5, 6, 2), MediaType.APPLICATION_JSON));

        assertExternalApiError(client(1));
        server.verify();
    }

    @Test
    void doesNotRetainApiKeyInTransportFailure() {
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/secret-test-key/json/getFcLckr/1/1000/"))
            .andRespond(withServerError());
        SeoulMetroLockerAvailabilityClient client = client("secret-test-key");
        Logger logger = (Logger) LoggerFactory.getLogger(SeoulMetroLockerAvailabilityClient.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            assertThatThrownBy(client::fetchAll)
                .isInstanceOfSatisfying(BusinessException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_API_ERROR);
                    assertThat(exception.getCause()).isNull();
                    assertThat(exception).hasMessageNotContaining("secret-test-key");
                });
            assertThat(appender.list).singleElement().satisfies(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.WARN);
                assertThat(event.getFormattedMessage())
                    .contains("reason=InternalServerError")
                    .doesNotContain("secret-test-key");
                assertThat(event.getThrowableProxy()).isNull();
            });
            server.verify();
        } finally {
            logger.detachAppender(appender);
        }
    }

    private String responseJson() {
        return """
            {
              "response": {
                "header": {"resultCode": "00", "resultMsg": "NORMAL_CODE"},
                "body": {
                  "items": {
                    "item": [{
                      "lckrDtlId": "TL124_DETAIL",
                      "usePsbltySmttypeLckrCnt": 12,
                      "usePsbltyMdtypeLckrCnt": 2,
                      "usePsbltyLrtypeLckrCnt": 0
                    }]
                  },
                  "totalCount": 1
                }
              }
            }
            """;
    }

    private String pageResponseJson(
        String externalLockerId,
        int smallAvailableCount,
        int mediumAvailableCount,
        int largeAvailableCount,
        int totalCount
    ) {
        return """
            {
              "response": {
                "header": {"resultCode": "00", "resultMsg": "NORMAL_CODE"},
                "body": {
                  "items": {
                    "item": [{
                      "lckrDtlId": "%s",
                      "usePsbltySmttypeLckrCnt": %d,
                      "usePsbltyMdtypeLckrCnt": %d,
                      "usePsbltyLrtypeLckrCnt": %d
                    }]
                  },
                  "totalCount": %d
                }
              }
            }
            """.formatted(
                externalLockerId,
                smallAvailableCount,
                mediumAvailableCount,
                largeAvailableCount,
                totalCount
            );
    }

    private void assertExternalApiError(SeoulMetroLockerAvailabilityClient client) {
        assertThatThrownBy(client::fetchAll)
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_API_ERROR));
    }

    private SeoulMetroLockerAvailabilityClient client() {
        return client("test-key");
    }

    private SeoulMetroLockerAvailabilityClient client(String apiKey) {
        return new SeoulMetroLockerAvailabilityClient(
            restClientBuilder.build(), new ObjectMapper(), apiKey
        );
    }

    private SeoulMetroLockerAvailabilityClient client(int pageSize) {
        return new SeoulMetroLockerAvailabilityClient(
            restClientBuilder.build(), new ObjectMapper(), "test-key", pageSize
        );
    }
}
