package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.NearbyLocker;
import com.zimdugo.locker.domain.NearbyLockerPlaceReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NearbyLockerPlaceReaderAdapter implements NearbyLockerPlaceReader {

    private final LockerRepository lockerRepository;

    @Override
    public List<NearbyLocker> findNearby(double latitude, double longitude, int radiusMeters) {
        List<NearbyLockerPlaceQueryProjection> nearbyLockers = lockerRepository.findNearbyLockers(
            latitude,
            longitude,
            radiusMeters
        );
        return nearbyLockers
            .stream()
            .map(NearbyLockerPlaceQueryProjection::toDomain)
            .toList();
    }
}
