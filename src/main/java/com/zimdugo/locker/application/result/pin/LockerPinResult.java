package com.zimdugo.locker.application.result.pin;

import java.util.List;

public record LockerPinResult(
    int count,
    List<LockerPinItemResult> items
) {
    public static LockerPinResult empty() {
        return new LockerPinResult(0, List.of());
    }

    public static LockerPinResult of(List<LockerPinItemResult> items) {
        return new LockerPinResult(items.size(), items);
    }
}
