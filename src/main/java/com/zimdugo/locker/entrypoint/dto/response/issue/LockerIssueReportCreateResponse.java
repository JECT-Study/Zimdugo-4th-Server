package com.zimdugo.locker.entrypoint.dto.response.issue;

import com.zimdugo.locker.application.result.issue.LockerIssueReportCreateResult;
import com.zimdugo.locker.domain.issue.LockerIssueReportStatus;
import com.zimdugo.locker.domain.issue.LockerIssueReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record LockerIssueReportCreateResponse(
    @Schema(description = "생성된 신고 ID", example = "17")
    Long reportId,
    @Schema(description = "신고 대상 보관함 ID", example = "123")
    Long lockerId,
    @Schema(description = "신고 유형", example = "OPERATING_HOURS_ERROR")
    LockerIssueReportType reportType,
    @Schema(description = "상세 신고 내용", example = "실제로는 오후 10시에 영업이 종료됩니다.")
    String detail,
    @Schema(description = "신고 처리 상태", example = "PENDING")
    LockerIssueReportStatus status,
    @Schema(description = "신고 접수 일시", example = "2026-08-19T20:30:15")
    LocalDateTime createdAt
) {
    public static LockerIssueReportCreateResponse from(LockerIssueReportCreateResult result) {
        return new LockerIssueReportCreateResponse(
            result.reportId(),
            result.lockerId(),
            result.reportType(),
            result.detail(),
            result.status(),
            result.createdAt()
        );
    }
}
