package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.locker.NearbyLocker;
import com.zimdugo.locker.domain.locker.NearbyLockerPlaceReader;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import com.zimdugo.locker.infrastructure.persistence.LockerBoundsFilter;
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
            neLng,
            toBoundsFilter(filter)
        );
        return nearbyLockers
            .stream()
            .map(NearbyLockerPlaceQueryProjection::toDomain)
            .toList();
    }

    private String filterValues(Iterable<? extends Enum<?>> filterValues) {
        if (filterValues == null) {
            return "";
        }
        StringBuilder values = new StringBuilder();
        for (Enum<?> filterValue : filterValues) {
            if (!values.isEmpty()) {
                values.append(',');
            }
            values.append(filterValue.name());
        }
        return values.toString();
    }

    private LockerBoundsFilter toBoundsFilter(LockerSearchFilter filter) {
        return new LockerBoundsFilter(
            filter != null && !filter.sizeTypes().isEmpty(),
            filterValues(filter == null ? null : filter.sizeTypes()),
            filter != null && !filter.indoorOutdoorTypes().isEmpty(),
            filterValues(filter == null ? null : filter.indoorOutdoorTypes()),
            filter != null && !filter.lockerTypes().isEmpty(),
            filterValues(filter == null ? null : filter.lockerTypes())
        );
    }
}
