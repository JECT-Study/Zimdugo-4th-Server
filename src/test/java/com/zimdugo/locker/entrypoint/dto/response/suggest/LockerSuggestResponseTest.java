package com.zimdugo.locker.entrypoint.dto.response.suggest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.common.config.JacksonConfig;
import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestResult;
import com.zimdugo.locker.application.result.LockerItemType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockerSuggestResponseTest {

    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    @Test
    @DisplayName("자동완성 아이템 유형은 type 필드로 노출한다")
    void exposesItemType() throws Exception {
        LockerSuggestResult result = LockerSuggestResult.of(List.of(new LockerSuggestItemResult(
            LockerItemType.LOCKER,
            101L,
            "신촌역",
            10L,
            "신촌역 보관함",
            "서울 서대문구",
            "SUBWAY_STATION",
            1000,
            37.55,
            126.93,
            95L,
            LocalDateTime.of(2026, 6, 7, 12, 0)
        )));

        JsonNode item = objectMapper.readTree(objectMapper.writeValueAsString(
            LockerSuggestResponse.from(result)
        )).path("items").get(0);

        assertThat(item.path("type").asText()).isEqualTo("LOCKER");
        assertThat(item.has("suggestType")).isFalse();
    }
}
