package com.zimdugo.locker.application.result.search;

import com.zimdugo.locker.application.result.GeoBoundsUtils;
import com.zimdugo.locker.application.result.LockerBoundsResult;
import com.zimdugo.locker.application.result.pin.LockerPinItemResult;
import java.util.List;

public record LockerSearchResult(
    int count,
    LockerBoundsResult bounds,
    List<LockerSearchItemResult> items,
    List<LockerPinItemResult> pins
) {
    public static LockerSearchResult empty() {
        return new LockerSearchResult(0, null, List.of(), List.of());
    }

    public static LockerSearchResult of(List<LockerSearchItemResult> items) {
        return of(items, List.of());
    }

    public static LockerSearchResult of(
        List<LockerSearchItemResult> items,
        List<LockerPinItemResult> pins
    ) {
        if (items == null || items.isEmpty()) {
            return empty();
        }

        LockerBoundsResult bounds = GeoBoundsUtils.from(
            items,
            LockerSearchItemResult::latitude,
            LockerSearchItemResult::longitude
        ).orElse(null);
        return new LockerSearchResult(items.size(), bounds, items, pins == null ? List.of() : pins);
    }
}
