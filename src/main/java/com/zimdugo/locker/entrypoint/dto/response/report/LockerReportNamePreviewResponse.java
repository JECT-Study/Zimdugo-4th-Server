package com.zimdugo.locker.entrypoint.dto.response.report;

import com.zimdugo.locker.application.result.report.LockerReportNamePreviewResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record LockerReportNamePreviewResponse(
    @Schema(description = "Resolved locker report name", example = "홍대입구역 2호선")
    String name,

    @Schema(description = "Road address", example = "서울 마포구 양화로 160")
    String roadAddress,

    @Schema(description = "Latitude", example = "37.556")
    double latitude,

    @Schema(description = "Longitude", example = "126.923")
    double longitude
) {
    public static LockerReportNamePreviewResponse from(LockerReportNamePreviewResult result) {
        return new LockerReportNamePreviewResponse(
            result.name(),
            result.roadAddress(),
            result.latitude(),
            result.longitude()
        );
    }
}
