package com.zimdugo.locker.domain.mypage;

import java.util.List;

public record MyLockerReportHistoryPage(
    List<MyLockerReportHistoryItem> items,
    long totalCount,
    boolean hasNext
) {
}
