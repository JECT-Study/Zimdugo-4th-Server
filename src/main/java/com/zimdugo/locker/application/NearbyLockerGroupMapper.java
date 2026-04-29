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
            // 그룹 대표 거리 기준으로 전체 그룹 정렬
            .sorted(Comparator.comparingLong(NearbyLockerGroupResponse::distanceMeters))
            .toList();
    }

    private NearbyLockerGroupResponse toGroupResponse(List<NearbyLocker> nearbyLockers) {
        // 그룹의 안에서는 반올림된 거리 기준으로 정렬
        List<NearbyLocker> sortedByDistance = nearbyLockers.stream()
            .sorted(Comparator.comparingLong(this::roundedDistance))
            .toList();

        // 정렬 후 첫번째 보관함을 그룹 대표 정보로 사용
        NearbyLocker nearestLocker = sortedByDistance.getFirst();
        List<NearbyLockerResponse> lockers = sortedByDistance.stream()
            .map(NearbyLockerResponse::from)
            .toList();

        return NearbyLockerGroupResponse.of(
            nearestLocker.latitude(),
            nearestLocker.longitude(),
            nearestLocker.roadAddress(),
            roundedDistance(nearestLocker),
            lockers
        );
    }

    private long roundedDistance(NearbyLocker nearbyLocker) {
        return Math.round(nearbyLocker.distanceMeters());
    }
}
