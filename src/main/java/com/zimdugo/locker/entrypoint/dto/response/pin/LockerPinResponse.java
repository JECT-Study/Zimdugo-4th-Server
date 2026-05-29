package com.zimdugo.locker.entrypoint.dto.response.pin;

import com.zimdugo.locker.application.result.pin.LockerPinResult;
import java.util.List;

public record LockerPinResponse(
    int count,
    List<LockerPinItemResponse> items
) {
    public static LockerPinResponse from(LockerPinResult result) {
        List<LockerPinItemResponse> items = result.items().stream()
            .map(LockerPinItemResponse::from)
            .toList();
        return new LockerPinResponse(result.count(), items);
    }
}
