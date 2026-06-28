package com.zimdugo.locker.entrypoint.dto.response.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.common.config.JacksonConfig;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.application.result.search.LockerSearchItemResult;
import com.zimdugo.locker.application.result.search.LockerSearchResult;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockerSearchResponseTest {

    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    @Test
    @DisplayName("search 응답은 bounds와 items를 노출하고 pins는 포함하지 않는다")
    void excludesPinsFromSearchResponse() throws Exception {
        LockerSearchResult result = LockerSearchResult.of(List.of(
            new LockerSearchItemResult(
                LockerItemType.LOCKER,
                101L,
                "신촌역 1번 출구",
                10L,
                "신촌역 1번 출구 보관함",
                "서울 서대문구 신촌역로 1",
                "SUBWAY_STATION",
                1000,
                37.551,
                126.936,
                95L,
                LocalDateTime.of(2026, 5, 31, 12, 0),
                false,
                List.of()
            )
        ));

        JsonNode root = objectMapper.readTree(objectMapper.writeValueAsString(
            RestResponse.of(SuccessCode.OK, LockerSearchResponse.from(result))
        ));

        JsonNode data = root.path("data");
        assertThat(data.path("count").asInt()).isEqualTo(1);
        assertThat(data.path("bounds").path("swLat").asDouble()).isEqualTo(37.551);
        assertThat(data.path("bounds").path("neLng").asDouble()).isEqualTo(126.936);
        assertThat(data.path("items")).hasSize(1);
        assertThat(data.has("pins")).isFalse();
    }

    @Test
    @DisplayName("빈 search 응답은 null bounds를 제외한다")
    void excludesNullBoundsWhenEmpty() throws Exception {
        JsonNode root = objectMapper.readTree(objectMapper.writeValueAsString(
            RestResponse.of(SuccessCode.OK, LockerSearchResponse.from(LockerSearchResult.empty()))
        ));

        assertThat(root.path("data").path("count").asInt()).isZero();
        assertThat(root.path("data").has("bounds")).isFalse();
        assertThat(root.path("data").path("items")).isEmpty();
        assertThat(root.path("data").has("pins")).isFalse();
    }
}
