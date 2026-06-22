package com.zimdugo.admin.dashboard;

import java.time.LocalDate;

public record AdminDashboardResult(
    LocalDate date,
    long todayVisitors,
    long todayReports,
    long pendingReports
) {
}
