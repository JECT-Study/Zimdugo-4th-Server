package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.keyword.LockerKeywordItemResult;
import com.zimdugo.locker.application.result.keyword.LockerKeywordLockerResult;
import com.zimdugo.locker.application.result.keyword.LockerKeywordResult;
import com.zimdugo.locker.domain.FavoriteLockerReader;
import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestType;
import com.zimdugo.locker.domain.LockerPlaceLocker;
import com.zimdugo.locker.domain.LockerPlaceLockerReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LockerKeywordQueryService {

    private final LockerSearchQueryService lockerSearchQueryService;
    private final LockerPlaceLockerReader lockerPlaceLockerReader;
    private final FavoriteLockerReader favoriteLockerReader;

    public LockerKeywordResult getKeywordResults(
        double latitude,
        double longitude,
        String keyword,
        int limit
    ) {
        return getKeywordResults(null, latitude, longitude, keyword, limit);
    }

    public LockerKeywordResult getKeywordResults(
        Long userId,
        double latitude,
        double longitude,
        String keyword,
        int limit
    ) {
        List<LockerSuggestItemResult> suggestItems = lockerSearchQueryService.search(
            latitude,
            longitude,
            keyword,
            limit
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
            : lockerPlaceLockerReader.readByPlaceIds(latitude, longitude, placeIds);
        Set<Long> favoriteLockerIds = resolveFavoriteLockerIds(userId, suggestItems, placeLockersByPlaceId);

        List<LockerKeywordItemResult> items = suggestItems.stream()
            .map(item -> toKeywordItem(item, placeLockersByPlaceId, favoriteLockerIds))
            .toList();
        return LockerKeywordResult.of(items);
    }

    private LockerKeywordItemResult toKeywordItem(
        LockerSuggestItemResult item,
        Map<Long, List<LockerPlaceLocker>> placeLockersByPlaceId,
        Set<Long> favoriteLockerIds
    ) {
        if (item.suggestType() == LockerSuggestType.LOCKER) {
            return LockerKeywordItemResult.locker(item, favoriteLockerIds.contains(item.lockerId()));
        }

        List<LockerKeywordLockerResult> lockers = placeLockersByPlaceId
            .getOrDefault(item.placeId(), List.of())
            .stream()
            .map(locker -> LockerKeywordLockerResult.from(
                locker,
                favoriteLockerIds.contains(locker.lockerId())
            ))
            .toList();
        return LockerKeywordItemResult.place(item, lockers);
    }

    private Set<Long> resolveFavoriteLockerIds(
        Long userId,
        List<LockerSuggestItemResult> suggestItems,
        Map<Long, List<LockerPlaceLocker>> placeLockersByPlaceId
    ) {
        if (userId == null) {
            return Set.of();
        }

        Set<Long> lockerIds = new HashSet<>();
        for (LockerSuggestItemResult item : suggestItems) {
            if (item.suggestType() == LockerSuggestType.LOCKER && item.lockerId() != null) {
                lockerIds.add(item.lockerId());
                continue;
            }
            for (LockerPlaceLocker locker : placeLockersByPlaceId.getOrDefault(item.placeId(), List.of())) {
                lockerIds.add(locker.lockerId());
            }
        }
        return favoriteLockerReader.findFavoriteLockerIds(userId, lockerIds);
    }
}
