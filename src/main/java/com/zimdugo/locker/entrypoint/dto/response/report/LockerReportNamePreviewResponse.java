package com.zimdugo.locker.entrypoint.dto.response.report;

import com.zimdugo.locker.application.result.report.LockerReportNamePreviewResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record LockerReportNamePreviewResponse(
    @Schema(description = "미리보기용 보관함 이름", example = "홍대입구역 2호선")
    String name,

    @Schema(description = "도로명 주소", example = "서울 마포구 양화로 160")
    String roadAddress,

    @Schema(description = "위도", example = "37.556")
    double latitude,

    @Schema(description = "경도", example = "126.923")
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
