package com.zimdugo.locker.domain.mypage;

import java.time.LocalDateTime;

public record MyLockerReportHistoryItem(
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
