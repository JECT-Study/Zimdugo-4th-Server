package com.zimdugo.locker.infrastructure.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.realtime.LockerRealtimeAvailabilitySnapshot;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SeoulMetroLockerAvailabilityClientTest {

    @Test
    void readsLockerAvailabilityFromCurrentSeoulOpenApiResponse() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://openapi.seoul.go.kr:8088");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/1/1000/"))
            .andRespond(withSuccess(responseJson(), MediaType.APPLICATION_JSON));
        SeoulMetroLockerAvailabilityClient client = new SeoulMetroLockerAvailabilityClient(
            builder.build(), new ObjectMapper(), "test-key"
        );

        List<LockerRealtimeAvailabilitySnapshot> lockers = client.fetchAll();

        assertThat(lockers).containsExactly(new LockerRealtimeAvailabilitySnapshot(
            "TL124_DETAIL", 12, 2, 0
        ));
        server.verify();
    }

    @Test
    void rejectsProviderErrorResponse() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://openapi.seoul.go.kr:8088");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/1/1000/"))
            .andRespond(withSuccess("""
                {
                  "response": {
                    "header": {"resultCode": "ERROR", "resultMsg": "INVALID_REQUEST"}
                  }
                }
                """, MediaType.APPLICATION_JSON));
        SeoulMetroLockerAvailabilityClient client = new SeoulMetroLockerAvailabilityClient(
            builder.build(), new ObjectMapper(), "test-key"
        );

        assertThatThrownBy(client::fetchAll)
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_API_ERROR));
        server.verify();
    }

    @Test
    void rejectsMalformedAvailabilityCount() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://openapi.seoul.go.kr:8088");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
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
        SeoulMetroLockerAvailabilityClient client = new SeoulMetroLockerAvailabilityClient(
            builder.build(), new ObjectMapper(), "test-key"
        );

        assertThatThrownBy(client::fetchAll)
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_API_ERROR));
        server.verify();
    }

    @Test
    void readsEveryPageReportedByTheProvider() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://openapi.seoul.go.kr:8088");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/1/1/"))
            .andRespond(withSuccess(pageResponseJson("TL1", 1, 2, 3, 2), MediaType.APPLICATION_JSON));
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/2/2/"))
            .andRespond(withSuccess(pageResponseJson("TL2", 4, 5, 6, 2), MediaType.APPLICATION_JSON));
        SeoulMetroLockerAvailabilityClient client = new SeoulMetroLockerAvailabilityClient(
            builder.build(), new ObjectMapper(), "test-key", 1
        );

        assertThat(client.fetchAll()).containsExactly(
            new LockerRealtimeAvailabilitySnapshot("TL1", 1, 2, 3),
            new LockerRealtimeAvailabilitySnapshot("TL2", 4, 5, 6)
        );
        server.verify();
    }

    @Test
    void rejectsIncompletePage() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://openapi.seoul.go.kr:8088");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/1/2/"))
            .andRespond(withSuccess(pageResponseJson("TL1", 1, 2, 3, 3), MediaType.APPLICATION_JSON));
        SeoulMetroLockerAvailabilityClient client = new SeoulMetroLockerAvailabilityClient(
            builder.build(), new ObjectMapper(), "test-key", 2
        );

        assertExternalApiError(client);
        server.verify();
    }

    @Test
    void rejectsDuplicateLockerIdAcrossPages() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://openapi.seoul.go.kr:8088");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/1/1/"))
            .andRespond(withSuccess(pageResponseJson("TL1", 1, 2, 3, 2), MediaType.APPLICATION_JSON));
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/test-key/json/getFcLckr/2/2/"))
            .andRespond(withSuccess(pageResponseJson("TL1", 4, 5, 6, 2), MediaType.APPLICATION_JSON));
        SeoulMetroLockerAvailabilityClient client = new SeoulMetroLockerAvailabilityClient(
            builder.build(), new ObjectMapper(), "test-key", 1
        );

        assertExternalApiError(client);
        server.verify();
    }

    @Test
    void doesNotRetainApiKeyInTransportFailure() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://openapi.seoul.go.kr:8088");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(request -> assertThat(request.getURI().getPath())
                .isEqualTo("/secret-test-key/json/getFcLckr/1/1000/"))
            .andRespond(withServerError());
        SeoulMetroLockerAvailabilityClient client = new SeoulMetroLockerAvailabilityClient(
            builder.build(), new ObjectMapper(), "secret-test-key"
        );

        assertThatThrownBy(client::fetchAll)
            .isInstanceOfSatisfying(BusinessException.class, exception -> {
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_API_ERROR);
                assertThat(exception.getCause()).isNull();
                assertThat(exception).hasMessageNotContaining("secret-test-key");
            });
        server.verify();
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
}
