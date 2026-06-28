package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.locker.NearbyLocker;
import com.zimdugo.locker.domain.locker.NearbyLockerPlaceReader;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.projection.NearbyLockerPlaceQueryProjection;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class NearbyLockerPlaceReaderAdapter implements NearbyLockerPlaceReader {

    private final LockerRepository lockerRepository;

    @Override
    public List<NearbyLocker> findWithinBounds(
        double swLat,
        double swLng,
        double neLat,
        double neLng,
        LockerSearchFilter filter
    ) {
        List<NearbyLockerPlaceQueryProjection> nearbyLockers = lockerRepository.findLockersWithinBounds(
            swLat,
            swLng,
            neLat,
            neLng
        );
        return nearbyLockers
            .stream()
            .filter(projection -> matchesFilter(projection, filter))
            .map(NearbyLockerPlaceQueryProjection::toDomain)
            .toList();
    }

    private boolean matchesFilter(
        NearbyLockerPlaceQueryProjection projection,
        LockerSearchFilter filter
    ) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }

        LockerType lockerType = LockerType.valueOf(projection.getLockerType());
        IndoorOutdoorType indoorOutdoorType = IndoorOutdoorType.valueOf(projection.getIndoorOutdoorType());
        Set<LockerSizeType> lockerSizes = parseLockerSizes(projection.getLockerSize());
        return filter.matches(lockerSizes, indoorOutdoorType, lockerType);
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
