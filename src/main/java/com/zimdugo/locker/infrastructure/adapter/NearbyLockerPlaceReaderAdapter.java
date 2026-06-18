package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.locker.NearbyLocker;
import com.zimdugo.locker.domain.locker.NearbyLockerPlaceReader;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.projection.NearbyLockerPlaceQueryProjection;
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
