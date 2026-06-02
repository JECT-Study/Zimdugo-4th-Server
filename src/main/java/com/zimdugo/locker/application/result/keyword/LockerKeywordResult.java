package com.zimdugo.locker.application.result.keyword;

import java.util.List;

public record LockerKeywordResult(
    int count,
    List<LockerKeywordItemResult> items
) {
    public static LockerKeywordResult empty() {
        return new LockerKeywordResult(0, List.of());
    }

    public static LockerKeywordResult of(List<LockerKeywordItemResult> items) {
        return new LockerKeywordResult(items.size(), items);
    }
}
