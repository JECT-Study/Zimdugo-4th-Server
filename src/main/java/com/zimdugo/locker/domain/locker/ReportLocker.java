package com.zimdugo.locker.domain.locker;

public record ReportLocker(
    Long id,
    String name,
    String roadAddress,
    double latitude,
    double longitude
) {
}
