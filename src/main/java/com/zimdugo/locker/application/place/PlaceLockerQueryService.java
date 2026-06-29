package com.zimdugo.locker.application.place;

import com.zimdugo.locker.application.filter.LockerSearchFilterFactory;
import com.zimdugo.common.i18n.CurrentRequestLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.result.search.LockerSearchLockerResult;
import com.zimdugo.locker.application.result.place.PlaceLockerResult;
import com.zimdugo.locker.domain.favorite.FavoriteLockerReader;
import com.zimdugo.locker.domain.place.LockerPlace;
import com.zimdugo.locker.domain.place.LockerPlaceLocker;
import com.zimdugo.locker.domain.place.LockerPlaceLockerReader;
import com.zimdugo.locker.domain.place.LockerPlaceReader;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlaceLockerQueryService {

    private final LockerPlaceReader lockerPlaceReader;
    private final LockerPlaceLockerReader lockerPlaceLockerReader;
    private final FavoriteLockerReader favoriteLockerReader;
    private final CurrentRequestLanguage currentRequestLanguage;

    public PlaceLockerResult getPlaceLockers(PlaceLockerQueryCommand command) {
        return getPlaceLockers(null, command);
    }

    public PlaceLockerResult getPlaceLockers(Long userId, PlaceLockerQueryCommand command) {
        String languageCode = currentRequestLanguage.resolve().languageTag();

        LockerPlace place = lockerPlaceReader.readById(command.placeId(), languageCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
        LockerSearchFilter filter = LockerSearchFilterFactory.create(
            command.sizeTypes(),
            command.indoorOutdoorTypes(),
            command.lockerTypes()
        );
        List<LockerPlaceLocker> lockers = lockerPlaceLockerReader.readByPlaceIds(
            command.latitude(),
            command.longitude(),
            List.of(command.placeId()),
            filter,
            languageCode
        ).getOrDefault(command.placeId(), List.of());
        Set<Long> favoriteLockerIds = resolveFavoriteLockerIds(userId, lockers);
        List<LockerSearchLockerResult> lockerResults = lockers.stream()
            .map(locker -> LockerSearchLockerResult.from(
                locker,
                favoriteLockerIds.contains(locker.lockerId())
            ))
            .toList();

        return PlaceLockerResult.of(place, lockerResults);
    }

    private Set<Long> resolveFavoriteLockerIds(Long userId, List<LockerPlaceLocker> lockers) {
        if (userId == null || lockers.isEmpty()) {
            return Set.of();
        }
        Set<Long> lockerIds = lockers.stream()
            .map(LockerPlaceLocker::lockerId)
            .collect(Collectors.toSet());
        return favoriteLockerReader.findFavoriteLockerIds(userId, lockerIds);
    }
}
