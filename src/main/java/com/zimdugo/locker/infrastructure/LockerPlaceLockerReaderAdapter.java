package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.LockerPlaceLocker;
import com.zimdugo.locker.domain.LockerPlaceLockerReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockerPlaceLockerReaderAdapter implements LockerPlaceLockerReader {

    private final LockerRepository lockerRepository;

    @Override
    public Map<Long, List<LockerPlaceLocker>> readByPlaceIds(
        double latitude,
        double longitude,
        List<Long> placeIds
    ) {
        if (placeIds == null || placeIds.isEmpty()) {
            return Map.of();
        }

        List<LockerPlaceLockerQueryProjection> projections = lockerRepository.findByPlaceIds(
            latitude,
            longitude,
            placeIds
        );

        Map<Long, List<LockerPlaceLocker>> lockersByPlace = new LinkedHashMap<>();
        for (LockerPlaceLockerQueryProjection projection : projections) {
            lockersByPlace.computeIfAbsent(projection.getPlaceId(), ignored -> new ArrayList<>())
                .add(toDomain(projection));
        }

        return lockersByPlace;
    }

    private LockerPlaceLocker toDomain(LockerPlaceLockerQueryProjection projection) {
        return new LockerPlaceLocker(
            projection.getPlaceId(),
            projection.getLockerId(),
            projection.getLockerName(),
            projection.getRoadAddress(),
            projection.getLockerType(),
            projection.getLockerLatitude(),
            projection.getLockerLongitude(),
            (long) projection.getDistanceMeters(),
            projection.getUpdatedAt()
        );
    }
}
