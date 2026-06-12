package com.zimdugo.locker.entrypoint.dto.response.mypage;

import com.zimdugo.locker.application.result.mypage.MyLockerReportHistoryItemResult;
import java.time.LocalDateTime;

public record MyLockerReportHistoryItemResponse(
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
    public static MyLockerReportHistoryItemResponse from(MyLockerReportHistoryItemResult item) {
        return new MyLockerReportHistoryItemResponse(
            item.reportId(),
            item.lockerName(),
            item.roadAddress(),
            item.lockerType(),
            item.imageUrl(),
            item.latitude(),
            item.longitude(),
            item.distanceMeters(),
            item.updatedAt()
        );
    }
}
