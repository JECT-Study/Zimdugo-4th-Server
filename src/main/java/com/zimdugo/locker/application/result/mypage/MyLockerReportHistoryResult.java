package com.zimdugo.locker.application.result.mypage;

import java.util.List;

public record MyLockerReportHistoryResult(
    int count,
    long totalCount,
    boolean hasNext,
    List<MyLockerReportHistoryItemResult> items
) {
    public static MyLockerReportHistoryResult of(
        List<MyLockerReportHistoryItemResult> items,
        long totalCount,
        boolean hasNext
    ) {
        return new MyLockerReportHistoryResult(items.size(), totalCount, hasNext, items);
    }
}
