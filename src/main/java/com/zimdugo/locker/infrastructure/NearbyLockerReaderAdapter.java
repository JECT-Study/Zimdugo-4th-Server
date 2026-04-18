package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.NearbyLocker;
import com.zimdugo.locker.domain.NearbyLockerReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NearbyLockerReaderAdapter implements NearbyLockerReader {

    private final LockerRepository lockerRepository;

    @Override
    public List<NearbyLocker> findNearby(double latitude, double longitude, int radiusMeters) {
        return lockerRepository.findNearby(latitude, longitude, radiusMeters)
            .stream()
            .map(this::toDomain)
            .toList();
    }

    private NearbyLocker toDomain(NearbyLockerQueryProjection projection) {
        return new NearbyLocker(
            projection.getId(),
            projection.getName(),
            projection.getRoadAddress(),
            projection.getLatitude(),
            projection.getLongitude(),
            projection.getDistanceMeters()
        );
    }
}
