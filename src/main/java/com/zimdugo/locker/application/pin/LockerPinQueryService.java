package com.zimdugo.locker.application.pin;

import com.zimdugo.locker.application.common.LocationValidator;

import com.zimdugo.locker.application.result.pin.LockerPinItemResult;
import com.zimdugo.locker.application.result.pin.LockerPinResult;
import com.zimdugo.locker.domain.favorite.FavoriteLockerReader;
import com.zimdugo.locker.domain.locker.NearbyLocker;
import com.zimdugo.locker.domain.locker.NearbyLockerPlaceReader;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerPinQueryService {

    private final NearbyLockerPlaceReader nearbyLockerPlaceReader;
    private final LockerPinAssembler lockerPinAssembler;
    private final LockerPinClusterer lockerPinClusterer;
    private final FavoriteLockerReader favoriteLockerReader;

    public LockerPinResult getPins(Long userId, LockerPinQuery query) {
        LocationValidator.validateBounds(query.swLat(), query.swLng(), query.neLat(), query.neLng());

        List<NearbyLocker> nearbyLockers = nearbyLockerPlaceReader.findWithinBounds(
            query.swLat(),
            query.swLng(),
            query.neLat(),
            query.neLng()
        );
        if (nearbyLockers.isEmpty()) {
            return LockerPinResult.empty();
        }

        Set<Long> favoriteLockerIds = resolveFavoriteLockerIds(userId, nearbyLockers);
        List<LockerPinItemResult> pins = lockerPinAssembler.assemble(nearbyLockers, favoriteLockerIds);
        return LockerPinResult.of(lockerPinClusterer.cluster(pins, query.zoomLevel()));
    }

    private Set<Long> resolveFavoriteLockerIds(Long userId, List<NearbyLocker> lockers) {
        if (userId == null) {
            return Set.of();
        }
        Set<Long> lockerIds = lockers.stream()
            .map(NearbyLocker::id)
            .collect(Collectors.toSet());
        return favoriteLockerReader.findFavoriteLockerIds(userId, lockerIds);
    }
}
