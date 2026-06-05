package com.zimdugo.locker.entrypoint.dto.response;

import com.zimdugo.locker.application.result.LockerBoundsResult;

public record LockerBoundsResponse(
    double swLat,
    double swLng,
    double neLat,
    double neLng
) {
    public static LockerBoundsResponse from(LockerBoundsResult bounds) {
        if (bounds == null) {
            return null;
        }

        return new LockerBoundsResponse(
            bounds.swLat(),
            bounds.swLng(),
            bounds.neLat(),
            bounds.neLng()
        );
    }
}
