package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.place.LockerPlaceLocker;
import com.zimdugo.locker.domain.place.LockerPlaceLockerReader;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.projection.LockerPlaceLockerQueryProjection;
import com.zimdugo.locker.infrastructure.projection.LockerSizeTypeQueryProjection;
import java.util.ArrayList;
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

        List<LockerPlaceLockerQueryProjection> projections =
            findProjections(latitude, longitude, placeIds, languageCode);
        Map<Long, Set<LockerSizeType>> lockerSizesByLockerId = findLockerSizes(projections);
        Map<Long, List<LockerPlaceLocker>> lockersByPlace = new LinkedHashMap<>();
        for (LockerPlaceLockerQueryProjection projection : projections) {
            LockerType lockerType = LockerType.valueOf(projection.getLockerType());
            IndoorOutdoorType indoorOutdoorType = IndoorOutdoorType.valueOf(projection.getIndoorOutdoorType());
            Set<LockerSizeType> lockerSizes = lockerSizesByLockerId.getOrDefault(projection.getLockerId(), Set.of());
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

    private Map<Long, Set<LockerSizeType>> findLockerSizes(List<LockerPlaceLockerQueryProjection> projections) {
        return lockerRepository.findLockerSizeTypesByLockerIds(
            projections.stream().map(LockerPlaceLockerQueryProjection::getLockerId).toList()
        ).stream().collect(Collectors.groupingBy(
            LockerSizeTypeQueryProjection::getLockerId,
            Collectors.mapping(
                projection -> LockerSizeType.from(projection.getSizeType()),
                Collectors.toUnmodifiableSet()
            )
        ));
    }
}
