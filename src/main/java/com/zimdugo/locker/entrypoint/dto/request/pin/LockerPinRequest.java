package com.zimdugo.locker.entrypoint.dto.request.pin;

import com.zimdugo.locker.application.pin.LockerPinQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record LockerPinRequest(
    @Parameter(description = "지도 남서쪽 위도", example = "37.490000")
    @Schema(minimum = "-90", maximum = "90")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    double swLat,

    @Parameter(description = "지도 남서쪽 경도", example = "127.020000")
    @Schema(minimum = "-180", maximum = "180")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    double swLng,

    @Parameter(description = "지도 북동쪽 위도", example = "37.510000")
    @Schema(minimum = "-90", maximum = "90")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    double neLat,

    @Parameter(description = "지도 북동쪽 경도", example = "127.040000")
    @Schema(minimum = "-180", maximum = "180")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    double neLng,

    @Parameter(description = "네이버 지도 줌 레벨. 15 이상은 상세 핀, 14 이하는 클러스터 적용", example = "14")
    @Schema(minimum = "0", maximum = "21")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "21.0")
    double zoom
) {
    public LockerPinQuery toQuery() {
        return new LockerPinQuery(swLat, swLng, neLat, neLng, zoom);
    }
}
