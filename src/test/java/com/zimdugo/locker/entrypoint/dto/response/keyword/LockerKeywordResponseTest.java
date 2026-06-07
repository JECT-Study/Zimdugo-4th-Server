package com.zimdugo.locker.entrypoint.dto.response.keyword;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.common.config.JacksonConfig;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.result.LockerBoundsResult;
import com.zimdugo.locker.application.result.keyword.LockerKeywordItemResult;
import com.zimdugo.locker.application.result.keyword.LockerKeywordResult;
import com.zimdugo.locker.application.result.LockerItemType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockerKeywordResponseTest {

    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    @Test
    @DisplayName("keyword 응답은 최상위 bounds를 sw/ne 좌표로 노출한다")
    void exposesTopLevelBounds() throws Exception {
        LockerKeywordResult result = new LockerKeywordResult(
            1,
            new LockerBoundsResult(37.551, 126.924, 37.557, 126.936),
            List.of(new LockerKeywordItemResult(
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
            ))
        );

        JsonNode root = objectMapper.readTree(objectMapper.writeValueAsString(
            RestResponse.of(SuccessCode.OK, LockerKeywordResponse.from(result))
        ));

        JsonNode bounds = root.path("data").path("bounds");
        assertThat(bounds.path("swLat").asDouble()).isEqualTo(37.551);
        assertThat(bounds.path("swLng").asDouble()).isEqualTo(126.924);
        assertThat(bounds.path("neLat").asDouble()).isEqualTo(37.557);
        assertThat(bounds.path("neLng").asDouble()).isEqualTo(126.936);
        assertThat(root.path("data").path("items")).hasSize(1);
        JsonNode item = root.path("data").path("items").get(0);
        assertThat(item.path("type").asText()).isEqualTo("LOCKER");
        assertThat(item.has("suggestType")).isFalse();
        assertThat(item.path("minPrice").asInt()).isEqualTo(1000);
    }

    @Test
    @DisplayName("빈 keyword 응답은 null인 bounds를 제외한다")
    void excludesNullBoundsWhenEmpty() throws Exception {
        JsonNode root = objectMapper.readTree(objectMapper.writeValueAsString(
            RestResponse.of(SuccessCode.OK, LockerKeywordResponse.from(LockerKeywordResult.empty()))
        ));

        assertThat(root.path("data").path("count").asInt()).isZero();
        assertThat(root.path("data").has("bounds")).isFalse();
        assertThat(root.path("data").path("items")).isEmpty();
    }
}
