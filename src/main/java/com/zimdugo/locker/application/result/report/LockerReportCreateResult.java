package com.zimdugo.locker.application.result.report;

import com.zimdugo.locker.application.report.LockerReportCreateCommand;
import com.zimdugo.locker.domain.report.SavedLockerReport;

public record LockerReportCreateResult(
    Long reportId,
    String roadAddress,
    double latitude,
    double longitude,
    String reportStatus
) {
    public static LockerReportCreateResult of(
        SavedLockerReport report,
        LockerReportCreateCommand command
    ) {
        return new LockerReportCreateResult(
            report.id(),
            command.roadAddress(),
            command.latitude(),
            command.longitude(),
            report.status()
        );
    }
}
