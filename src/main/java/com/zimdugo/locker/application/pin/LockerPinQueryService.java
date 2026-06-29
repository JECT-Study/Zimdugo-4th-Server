package com.zimdugo.locker.application.pin;

import com.zimdugo.locker.application.filter.LockerSearchFilterFactory;
import com.zimdugo.locker.application.common.LocationValidator;
import com.zimdugo.locker.application.search.LockerSearchResultQueryService;
import com.zimdugo.locker.application.result.search.LockerSearchItemResult;
import com.zimdugo.locker.application.result.pin.LockerPinItemResult;
import com.zimdugo.locker.application.result.pin.LockerPinResult;
import com.zimdugo.locker.domain.favorite.FavoriteLockerReader;
import com.zimdugo.locker.domain.locker.NearbyLocker;
import com.zimdugo.locker.domain.locker.NearbyLockerPlaceReader;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
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
    private final LockerSearchPinAssembler lockerSearchPinAssembler;
    private final LockerPinClusterer lockerPinClusterer;
    private final FavoriteLockerReader favoriteLockerReader;
    private final LockerSearchResultQueryService lockerSearchResultQueryService;

    public LockerPinResult getPins(Long userId, LockerPinQuery query) {
        LocationValidator.validateBounds(query.swLat(), query.swLng(), query.neLat(), query.neLng()); //좌표 검증

        LockerSearchFilter filter = LockerSearchFilterFactory.create(
            query.sizeTypes(),
            query.indoorOutdoorTypes(),
            query.lockerTypes()
        );

        if (query.hasKeywordSearch()) {
            return getSearchPins(userId, query, filter);
        }

        List<NearbyLocker> nearbyLockers = nearbyLockerPlaceReader.findWithinBounds(
            query.swLat(),
            query.swLng(),
            query.neLat(),
            query.neLng(),
            filter
        );
        if (nearbyLockers.isEmpty()) {
            return LockerPinResult.empty();
        }

        Set<Long> favoriteLockerIds = resolveFavoriteLockerIds(userId, nearbyLockers);
        List<LockerPinItemResult> pins = lockerPinAssembler.assemble(nearbyLockers, favoriteLockerIds);
        return LockerPinResult.of(lockerPinClusterer.cluster(pins, query.zoomLevel()));
    }

    private LockerPinResult getSearchPins(Long userId, LockerPinQuery query, LockerSearchFilter filter) {
        if (query.userLat() == null || query.userLng() == null) {
            return LockerPinResult.empty();
        }

        List<LockerSearchItemResult> items = lockerSearchResultQueryService.getDisplayableSearchItemsForPins(
            userId,
            query.userLat(),
            query.userLng(),
            query.keyword(),
            filter
        );
        if (items.isEmpty()) {
            return LockerPinResult.empty();
        }

        List<LockerPinItemResult> visiblePins = lockerSearchPinAssembler.assemble(items).stream()
            .filter(pin -> isWithinBounds(pin, query))
            .toList();
        if (visiblePins.isEmpty()) {
            return LockerPinResult.empty();
        }

        return LockerPinResult.of(lockerPinClusterer.cluster(visiblePins, query.zoomLevel()));
    }

    private boolean isWithinBounds(LockerPinItemResult pin, LockerPinQuery query) {
        return pin.latitude() >= query.swLat()
            && pin.latitude() <= query.neLat()
            && pin.longitude() >= query.swLng()
            && pin.longitude() <= query.neLng();
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
