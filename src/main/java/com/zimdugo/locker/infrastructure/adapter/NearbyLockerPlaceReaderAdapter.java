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
    public List<NearbyLocker> findWithinBounds(double swLat, double swLng, double neLat, double neLng) {
        List<NearbyLockerPlaceQueryProjection> nearbyLockers = lockerRepository.findLockersWithinBounds(
            swLat,
            swLng,
            neLat,
            neLng
        );
        return nearbyLockers
            .stream()
            .map(NearbyLockerPlaceQueryProjection::toDomain)
            .toList();
    }
}
