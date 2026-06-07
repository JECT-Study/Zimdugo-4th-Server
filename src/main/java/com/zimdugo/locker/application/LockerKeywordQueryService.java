package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.keyword.LockerKeywordItemResult;
import com.zimdugo.locker.application.result.keyword.LockerKeywordLockerResult;
import com.zimdugo.locker.application.result.keyword.LockerKeywordResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestType;
import com.zimdugo.locker.domain.LockerPlaceLocker;
import com.zimdugo.locker.domain.LockerPlaceLockerReader;
import com.zimdugo.locker.domain.LockerSearchFilter;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LockerKeywordQueryService {

    private final LockerSearchQueryService lockerSearchQueryService;
    private final LockerPlaceLockerReader lockerPlaceLockerReader;

    public LockerKeywordResult getKeywordResults(LockerKeywordSearchCommand command) {
        return getKeywordResults(
            command.latitude(),
            command.longitude(),
            command.keyword(),
            command.limit(),
            LockerSearchFilter.from(command.sizeTypes(), command.indoorOutdoorType(), command.lockerType())
        );
    }

    public LockerKeywordResult getKeywordResults(
        double latitude,
        double longitude,
        String keyword,
        int limit,
        LockerSearchFilter filter
    ) {
        List<LockerSuggestItemResult> suggestItems = lockerSearchQueryService.search(
            latitude,
            longitude,
            keyword,
            limit,
            filter
        );
        if (suggestItems.isEmpty()) {
            return LockerKeywordResult.empty();
        }

        List<Long> placeIds = suggestItems.stream()
            .filter(item -> item.suggestType() == LockerSuggestType.PLACE)
            .map(LockerSuggestItemResult::placeId)
            .toList();
        Map<Long, List<LockerPlaceLocker>> placeLockersByPlaceId = placeIds.isEmpty()
            ? Map.of()
            : lockerPlaceLockerReader.readByPlaceIds(latitude, longitude, placeIds, filter);

        List<LockerKeywordItemResult> items = suggestItems.stream()
            .map(item -> toKeywordItem(item, placeLockersByPlaceId))
            .toList();
        return LockerKeywordResult.of(items);
    }

    private LockerKeywordItemResult toKeywordItem(
        LockerSuggestItemResult item,
        Map<Long, List<LockerPlaceLocker>> placeLockersByPlaceId
    ) {
        if (item.suggestType() == LockerSuggestType.LOCKER) {
            return LockerKeywordItemResult.locker(item);
        }

        List<LockerKeywordLockerResult> lockers = placeLockersByPlaceId
            .getOrDefault(item.placeId(), List.of())
            .stream()
            .map(LockerKeywordLockerResult::from)
            .toList();
        return LockerKeywordItemResult.place(item, lockers);
    }
}
