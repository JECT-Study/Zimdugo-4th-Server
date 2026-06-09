package com.zimdugo.locker.entrypoint.dto.request.keyword;

import com.zimdugo.locker.application.LockerKeywordSearchCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record LockerKeywordRequest(
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

    @Schema(description = "검색 키워드", example = "신촌역 1번 출구")
    @NotBlank
    @Size(max = 100)
    String keyword,

    @Schema(description = "보관함 크기 필터, 복수 선택 가능", example = "[\"SMALL\", \"MEDIUM\", \"LARGE\"]")
    Set<String> sizeTypes,

    @Schema(description = "실내/실외 필터, 복수 선택 가능", example = "[\"INDOOR\", \"OUTDOOR\"]")
    Set<String> indoorOutdoorTypes,

    @Schema(
        description = "보관함 유형 필터 (MUSEUM, SUBWAY_STATION, DEPARTMENT_STORE, CONVENIENCE_STORE, "
            + "PUBLIC_OFFICE, PRIVATE_LOCKER, TRAIN_STATION, ETC), 복수 선택 가능",
        example = "[\"MUSEUM\", \"SUBWAY_STATION\", \"DEPARTMENT_STORE\", \"CONVENIENCE_STORE\", "
            + "\"PUBLIC_OFFICE\", \"PRIVATE_LOCKER\", \"TRAIN_STATION\", \"ETC\"]"
    )
    Set<String> lockerTypes
) {

    public LockerKeywordSearchCommand toCommand() {
        return new LockerKeywordSearchCommand(
            lat,
            lng,
            keyword,
            sizeTypes,
            indoorOutdoorTypes,
            lockerTypes
        );
    }
}
