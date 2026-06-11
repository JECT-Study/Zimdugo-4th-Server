package com.zimdugo.locker.application.result.mypage;

import java.time.LocalDateTime;

public record MyLockerReportHistoryItemResult(
    Long reportId,
    String lockerName,
    String roadAddress,
    String lockerType,
    String imageUrl,
    double latitude,
    double longitude,
    long distanceMeters,
    LocalDateTime updatedAt
) {
}
