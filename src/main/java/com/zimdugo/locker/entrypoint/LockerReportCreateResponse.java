package com.zimdugo.locker.entrypoint;

import io.swagger.v3.oas.annotations.media.Schema;

public record LockerReportCreateResponse(
    @Schema(description = "제보 ID", example = "10")
    Long reportId,

    @Schema(description = "제보가 연결된 보관함 ID", example = "1")
    Long lockerId,

    @Schema(description = "보관함 이름", example = "홍대입구역 보관함")
    String name,

    @Schema(description = "도로명 주소", example = "서울 마포구 양화로 160")
    String roadAddress,

    @Schema(description = "위도", example = "37.556")
    double latitude,

    @Schema(description = "경도", example = "126.923")
    double longitude,

    @Schema(description = "제보 상태", example = "COMPLETED")
    String reportStatus
) {
}
