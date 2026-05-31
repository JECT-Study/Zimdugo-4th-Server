package com.zimdugo.locker.application.result.suggest;

import java.util.List;

public record LockerSuggestResult(
    int count,
    List<LockerSuggestItemResult> items
) {
    public static LockerSuggestResult empty() {
        return new LockerSuggestResult(0, List.of());
    }

    public static LockerSuggestResult of(List<LockerSuggestItemResult> items) {
        return new LockerSuggestResult(items.size(), items);
    }
}
