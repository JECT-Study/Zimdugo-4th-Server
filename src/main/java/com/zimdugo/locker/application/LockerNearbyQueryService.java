package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.NearbyLockerReader;
import com.zimdugo.locker.domain.NearbyLocker;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerNearbyQueryService {

    private final NearbyLockerReader nearbyLockerReader;
    private final NearbyLockerGrouper nearbyLockerGrouper;
    private final NearbyLockerGroupMapper nearbyLockerGroupMapper;

    /**
     * 조회 -> 그룹화 시키고 -> 매핑
     * */
    public List<NearbyLockerGroupResponse> getNearbyLockerGroups(double latitude, double longitude, int radiusMeters) {
        List<NearbyLocker> nearbyLockers = nearbyLockerReader.findNearby(
            latitude, longitude, radiusMeters
        );
        if (nearbyLockers.isEmpty()) {
            return List.of();
        }

        List<List<NearbyLocker>> groupedLockers = nearbyLockerGrouper.groupByCoordinateOrRoadAddress(nearbyLockers);
        return nearbyLockerGroupMapper.toGroupResponses(groupedLockers);
    }
}
