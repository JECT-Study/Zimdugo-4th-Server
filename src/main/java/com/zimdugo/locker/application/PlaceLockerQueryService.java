package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.result.keyword.LockerKeywordLockerResult;
import com.zimdugo.locker.application.result.place.PlaceLockerResult;
import com.zimdugo.locker.domain.LockerPlace;
import com.zimdugo.locker.domain.LockerPlaceLocker;
import com.zimdugo.locker.domain.LockerPlaceLockerReader;
import com.zimdugo.locker.domain.LockerPlaceReader;
import com.zimdugo.locker.domain.LockerSearchFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlaceLockerQueryService {

    private final LockerPlaceReader lockerPlaceReader;
    private final LockerPlaceLockerReader lockerPlaceLockerReader;

    public PlaceLockerResult getPlaceLockers(PlaceLockerQueryCommand command) {
        LockerPlace place = lockerPlaceReader.readById(command.placeId())
            .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
        LockerSearchFilter filter = LockerSearchFilter.from(
            command.sizeTypes(),
            command.indoorOutdoorType(),
            command.lockerType()
        );
        List<LockerPlaceLocker> lockers = lockerPlaceLockerReader.readByPlaceIds(
            command.latitude(),
            command.longitude(),
            List.of(command.placeId()),
            filter
        ).getOrDefault(command.placeId(), List.of());
        List<LockerKeywordLockerResult> lockerResults = lockers.stream()
            .map(LockerKeywordLockerResult::from)
            .toList();

        return PlaceLockerResult.of(place, lockerResults);
    }
}
