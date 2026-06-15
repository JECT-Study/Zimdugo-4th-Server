package com.zimdugo.locker.entrypoint.dto.request.report;

import com.zimdugo.locker.application.LockerReportNamePreviewCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LockerReportNamePreviewRequest(
    @Schema(description = "도로명 주소", example = "서울 마포구 양화로 160")
    @NotBlank
    @Size(max = 255)
    String roadAddress,

    @Schema(description = "위도", example = "37.556")
    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    Double lat,

    @Schema(description = "경도", example = "126.923")
    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double lng,

    @Schema(description = "보관함 유형", example = "SUBWAY_STATION")
    @NotBlank
    @Size(max = 20)
    String lockerType
) {
    public LockerReportNamePreviewCommand toCommand() {
        return new LockerReportNamePreviewCommand(
            roadAddress,
            lat,
            lng,
            lockerType
        );
    }
}
