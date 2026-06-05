package com.zimdugo.locker.application.result.keyword;

import com.zimdugo.locker.application.result.GeoBoundsUtils;
import com.zimdugo.locker.application.result.LockerBoundsResult;
import java.util.List;

public record LockerKeywordResult(
    int count,
    LockerBoundsResult bounds,
    List<LockerKeywordItemResult> items
) {
    public static LockerKeywordResult empty() {
        return new LockerKeywordResult(0, null, List.of());
    }

    public static LockerKeywordResult of(List<LockerKeywordItemResult> items) {
        if (items == null || items.isEmpty()) {
            return empty();
        }

        LockerBoundsResult bounds = GeoBoundsUtils.from(
            items,
            LockerKeywordItemResult::latitude,
            LockerKeywordItemResult::longitude
        ).orElse(null);
        return new LockerKeywordResult(items.size(), bounds, items);
    }
}
