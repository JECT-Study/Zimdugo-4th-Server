package com.zimdugo.locker.entrypoint.dto.response.pin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.common.config.JacksonConfig;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.result.LockerBoundsResult;
import com.zimdugo.locker.application.result.pin.LockerPinItemResult;
import com.zimdugo.locker.application.result.pin.LockerPinResult;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockerPinResponseTest {

    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    @Test
    @DisplayName("CLUSTER 핀 응답은 pinCount와 bounds를 노출한다")
    void exposesClusterPinCountAndBounds() throws Exception {
        LockerPinResult result = LockerPinResult.of(List.of(
            LockerPinItemResult.cluster(
                37.501,
                127.021,
                3,
                new LockerBoundsResult(37.500, 127.020, 37.502, 127.022)
            )
        ));

        JsonNode root = objectMapper.readTree(objectMapper.writeValueAsString(
            RestResponse.of(SuccessCode.OK, LockerPinResponse.from(result))
        ));

        JsonNode item = root.path("data").path("items").get(0);
        assertThat(item.path("pinType").asText()).isEqualTo("CLUSTER");
        assertThat(item.path("pinCount").asInt()).isEqualTo(3);
        assertThat(item.path("bounds").path("swLat").asDouble()).isEqualTo(37.500);
        assertThat(item.path("bounds").path("swLng").asDouble()).isEqualTo(127.020);
        assertThat(item.path("bounds").path("neLat").asDouble()).isEqualTo(37.502);
        assertThat(item.path("bounds").path("neLng").asDouble()).isEqualTo(127.022);
    }

}
