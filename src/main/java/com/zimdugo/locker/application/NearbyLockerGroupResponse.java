package com.zimdugo.locker.application;

import java.util.List;

public record NearbyLockerGroupResponse(
    double latitude,
    double longitude,
    String roadAddress,
    long distanceMeters,
    int lockerCount,
    List<NearbyLockerResponse> lockers
) {
    public static NearbyLockerGroupResponse of(
        double latitude,
        double longitude,
        String roadAddress,
        long distanceMeters,
        List<NearbyLockerResponse> lockers
    ) {
        return new NearbyLockerGroupResponse(
            latitude,
            longitude,
            roadAddress,
            distanceMeters,
            lockers.size(),
            lockers
        );
    }
}
