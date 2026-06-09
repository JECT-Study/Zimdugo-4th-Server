package com.zimdugo.locker.entrypoint.dto.response.mypage;

import com.zimdugo.locker.application.result.mypage.MyLockerReportHistoryResult;
import java.util.List;

public record MyLockerReportHistoryResponse(
    int count,
    long totalCount,
    boolean hasNext,
    List<MyLockerReportHistoryItemResponse> items
) {
    public static MyLockerReportHistoryResponse from(MyLockerReportHistoryResult result) {
        List<MyLockerReportHistoryItemResponse> items = result.items().stream()
            .map(MyLockerReportHistoryItemResponse::from)
            .toList();

        return new MyLockerReportHistoryResponse(
            result.count(),
            result.totalCount(),
            result.hasNext(),
            items
        );
    }
}
