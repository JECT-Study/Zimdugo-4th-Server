package com.zimdugo.locker.entrypoint.dto.response.issue;

import com.zimdugo.locker.application.result.issue.LockerIssueReportCreateResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record LockerIssueReportCreateResponse(
    @Schema(description = "생성된 신고 ID", example = "17")
    Long reportId,
    @Schema(description = "신고 대상 보관함 ID", example = "123")
    Long lockerId,
    @Schema(description = "신고 유형", example = "OPERATING_HOURS_ERROR")
    String reportType,
    @Schema(description = "신고 처리 상태", example = "PENDING")
    String status,
    @Schema(description = "신고 접수 일시", example = "2026-08-19T20:30:15")
    LocalDateTime createdAt
) {
    public static LockerIssueReportCreateResponse from(LockerIssueReportCreateResult result) {
        return new LockerIssueReportCreateResponse(
            result.reportId(),
            result.lockerId(),
            result.reportType(),
            result.status(),
            result.createdAt()
        );
    }
}
