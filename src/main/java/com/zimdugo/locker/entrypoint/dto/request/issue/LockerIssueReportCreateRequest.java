package com.zimdugo.locker.entrypoint.dto.request.issue;

import com.zimdugo.locker.application.issue.LockerIssueReportCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LockerIssueReportCreateRequest(
    @Schema(
        description = "신고 유형",
        allowableValues = {
            "PRICE_ERROR",
            "NO_LONGER_OPERATING",
            "SIZE_ERROR",
            "OPERATING_HOURS_ERROR",
            "WRONG_LOCATION",
            "IMAGE_ERROR",
            "CATEGORY_ERROR",
            "OTHER"
        },
        example = "OPERATING_HOURS_ERROR"
    )
    @NotBlank
    @Pattern(regexp = REPORT_TYPE_PATTERN)
    String reportType,

    @Schema(description = "상세 내용", example = "운영 시간이 실제와 다릅니다.")
    @Size(max = 1000)
    String detail
) {
    private static final String REPORT_TYPE_PATTERN =
        "PRICE_ERROR|NO_LONGER_OPERATING|SIZE_ERROR|OPERATING_HOURS_ERROR|"
            + "WRONG_LOCATION|IMAGE_ERROR|CATEGORY_ERROR|OTHER";

    public LockerIssueReportCreateCommand toCommand(Long lockerId) {
        return new LockerIssueReportCreateCommand(lockerId, reportType, detail);
    }
}
