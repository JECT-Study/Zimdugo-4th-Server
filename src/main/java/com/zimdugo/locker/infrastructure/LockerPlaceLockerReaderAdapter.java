package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.LockerPlaceLocker;
import com.zimdugo.locker.domain.LockerPlaceLockerReader;
import com.zimdugo.locker.domain.IndoorOutdoorType;
import com.zimdugo.locker.domain.LockerSearchFilter;
import com.zimdugo.locker.domain.LockerSizeType;
import com.zimdugo.locker.domain.LockerType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
        LockerSearchFilter filter,
        String languageCode
    ) {
        if (placeIds == null || placeIds.isEmpty()) {
            return Map.of();
        }

        List<LockerPlaceLockerQueryProjection> projections = findProjections(latitude, longitude, placeIds, languageCode);
        Map<Long, List<LockerPlaceLocker>> lockersByPlace = new LinkedHashMap<>();
        for (LockerPlaceLockerQueryProjection projection : projections) {
            LockerType lockerType = LockerType.valueOf(projection.getLockerType());
            IndoorOutdoorType indoorOutdoorType = IndoorOutdoorType.valueOf(projection.getIndoorOutdoorType());
            Set<LockerSizeType> lockerSizes = parseLockerSizes(projection.getLockerSize());
            if (!filter.matches(lockerSizes, indoorOutdoorType, lockerType)) {
                continue;
            }
            lockersByPlace.computeIfAbsent(projection.getPlaceId(), ignored -> new ArrayList<>())
                .add(toDomain(
                    projection,
                    lockerType,
                    indoorOutdoorType,
                    lockerSizes
                ));
        }
        return lockersByPlace;
    }

    private List<LockerPlaceLockerQueryProjection> findProjections(
        double latitude,
        double longitude,
        List<Long> placeIds,
        String languageCode
    ) {
        return lockerRepository.findByPlaceIds(latitude, longitude, placeIds, languageCode);
    }

    private LockerPlaceLocker toDomain(
        LockerPlaceLockerQueryProjection projection,
        LockerType lockerType,
        IndoorOutdoorType indoorOutdoorType,
        Set<LockerSizeType> lockerSizes
    ) {
        return new LockerPlaceLocker(
            projection.getPlaceId(),
            projection.getLockerId(),
            projection.getLockerName(),
            projection.getRoadAddress(),
            lockerType,
            indoorOutdoorType,
            lockerSizes,
            projection.getMinPrice(),
            projection.getLockerLatitude(),
            projection.getLockerLongitude(),
            (long) projection.getDistanceMeters(),
            projection.getUpdatedAt()
        );
    }

    private Set<LockerSizeType> parseLockerSizes(String lockerSizes) {
        if (lockerSizes == null || lockerSizes.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(lockerSizes.split(","))
            .map(LockerSizeType::from)
            .collect(Collectors.toUnmodifiableSet());
    }
}
