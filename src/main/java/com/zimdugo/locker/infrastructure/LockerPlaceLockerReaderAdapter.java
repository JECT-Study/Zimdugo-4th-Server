package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.LockerPlaceLocker;
import com.zimdugo.locker.domain.LockerPlaceLockerReader;
import com.zimdugo.locker.domain.IndoorOutdoorType;
import com.zimdugo.locker.domain.LockerSearchFilter;
import com.zimdugo.locker.domain.LockerSizeType;
import com.zimdugo.locker.domain.LockerType;
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
        List<Long> placeIds,
        LockerSearchFilter filter
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
            LockerType lockerType = LockerType.valueOf(projection.getLockerType());
            IndoorOutdoorType indoorOutdoorType = IndoorOutdoorType.valueOf(projection.getIndoorOutdoorType());
            LockerSizeType lockerSize = LockerSizeType.from(projection.getLockerSize());
            if (!filter.matches(lockerSize, indoorOutdoorType, lockerType)) {
                continue;
            }
            lockersByPlace.computeIfAbsent(projection.getPlaceId(), ignored -> new ArrayList<>())
                .add(toDomain(projection, lockerType, indoorOutdoorType, lockerSize));
        }

        return lockersByPlace;
    }

    private LockerPlaceLocker toDomain(
        LockerPlaceLockerQueryProjection projection,
        LockerType lockerType,
        IndoorOutdoorType indoorOutdoorType,
        LockerSizeType lockerSize
    ) {
        return new LockerPlaceLocker(
            projection.getPlaceId(),
            projection.getLockerId(),
            projection.getLockerName(),
            projection.getRoadAddress(),
            lockerType,
            indoorOutdoorType,
            lockerSize,
            projection.getLockerLatitude(),
            projection.getLockerLongitude(),
            (long) projection.getDistanceMeters(),
            projection.getUpdatedAt()
        );
    }
}
