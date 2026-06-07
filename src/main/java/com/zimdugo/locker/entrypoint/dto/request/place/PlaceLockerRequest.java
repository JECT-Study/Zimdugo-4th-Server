package com.zimdugo.locker.entrypoint.dto.request.place;

import com.zimdugo.locker.application.PlaceLockerQueryCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record PlaceLockerRequest(
    @Schema(description = "사용자 위도", example = "37.498095", minimum = "-90", maximum = "90")
    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    Double lat,

    @Schema(description = "사용자 경도", example = "127.027610", minimum = "-180", maximum = "180")
    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double lng,

    @Schema(description = "보관함 크기 필터, 복수 선택 가능", example = "[\"SMALL\", \"MEDIUM\", \"BIG\"]")
    Set<String> sizeTypes,

    @Schema(description = "실내/실외 필터", example = "INDOOR")
    String indoorOutdoorType,

    @Schema(description = "보관함 유형 필터", example = "SUBWAY_STATION")
    String lockerType
) {

    public PlaceLockerQueryCommand toCommand(Long placeId) {
        return new PlaceLockerQueryCommand(
            placeId,
            lat,
            lng,
            sizeTypes,
            indoorOutdoorType,
            lockerType
        );
    }
}
