package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.NearbyLocker;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NearbyLockerGroupMapper {

    public List<NearbyLockerGroupResponse> toGroupResponses(List<List<NearbyLocker>> groupedLockers) {
        return groupedLockers.stream()
            .map(this::toGroupResponse)
            .sorted(Comparator.comparingLong(NearbyLockerGroupResponse::distanceMeters))
            .toList();
    }

    private NearbyLockerGroupResponse toGroupResponse(List<NearbyLocker> nearbyLockers) {
        List<NearbyLocker> sortedByDistance = nearbyLockers.stream()
            .sorted(Comparator.comparingDouble(NearbyLocker::distanceMeters))
            .toList();

        NearbyLocker nearestLocker = sortedByDistance.getFirst();
        List<NearbyLockerResponse> lockers = sortedByDistance.stream()
            .map(NearbyLockerResponse::from)
            .toList();

        return NearbyLockerGroupResponse.of(
            nearestLocker.latitude(),
            nearestLocker.longitude(),
            nearestLocker.roadAddress(),
            Math.round(nearestLocker.distanceMeters()),
            lockers
        );
    }
}
