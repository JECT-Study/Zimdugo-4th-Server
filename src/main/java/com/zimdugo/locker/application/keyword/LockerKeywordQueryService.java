package com.zimdugo.locker.application.keyword;

import com.zimdugo.common.i18n.CurrentRequestLanguage;
import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.application.result.keyword.LockerKeywordItemResult;
import com.zimdugo.locker.application.result.keyword.LockerKeywordLockerResult;
import com.zimdugo.locker.application.result.keyword.LockerKeywordResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.application.search.LockerSearchQueryService;
import com.zimdugo.locker.domain.favorite.FavoriteLockerReader;
import com.zimdugo.locker.domain.place.LockerPlaceLocker;
import com.zimdugo.locker.domain.place.LockerPlaceLockerReader;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LockerKeywordQueryService {

    private final LockerSearchQueryService lockerSearchQueryService;
    private final KeywordCountCommandService keywordCountCommandService;
    private final LockerPlaceLockerReader lockerPlaceLockerReader;
    private final FavoriteLockerReader favoriteLockerReader;
    private final CurrentRequestLanguage currentRequestLanguage;

    public LockerKeywordResult getKeywordResults(LockerKeywordSearchCommand command) {
        return getKeywordResults(null, command);
    }

    public LockerKeywordResult getKeywordResults(Long userId, LockerKeywordSearchCommand command) {
        return getKeywordResults(
            userId,
            command.latitude(),
            command.longitude(),
            command.keyword(),
            LockerSearchFilter.from(command.sizeTypes(), command.indoorOutdoorTypes(), command.lockerTypes())
        );
    }

    public LockerKeywordResult getKeywordResults(
        double latitude,
        double longitude,
        String keyword,
        LockerSearchFilter filter
    ) {
        return getKeywordResults(null, latitude, longitude, keyword, filter);
    }

    public LockerKeywordResult getKeywordResults(
        Long userId,
        double latitude,
        double longitude,
        String keyword,
        LockerSearchFilter filter
    ) {
        if (filter == null) {
            filter = LockerSearchFilter.empty();
        }
        increaseKeywordCount(keyword);
        List<LockerSuggestItemResult> suggestItems = lockerSearchQueryService.search(
            latitude,
            longitude,
            keyword,
            filter
        );
        if (suggestItems.isEmpty()) {
            return LockerKeywordResult.empty();
        }

        List<Long> placeIds = suggestItems.stream()
            .filter(item -> item.type() == LockerItemType.PLACE)
            .map(LockerSuggestItemResult::placeId)
            .toList();
        Map<Long, List<LockerPlaceLocker>> placeLockersByPlaceId =
            readPlaceLockers(latitude, longitude, placeIds, filter);
        Set<Long> targetLockerIds = collectTargetLockerIds(suggestItems, placeLockersByPlaceId);
        Set<Long> favoriteLockerIds = resolveFavoriteLockerIds(userId, targetLockerIds);

        List<LockerKeywordItemResult> items = suggestItems.stream()
            .map(item -> toKeywordItem(item, placeLockersByPlaceId, favoriteLockerIds))
            .toList();
        return LockerKeywordResult.of(items);
    }

    private Map<Long, List<LockerPlaceLocker>> readPlaceLockers(
        double latitude,
        double longitude,
        List<Long> placeIds,
        LockerSearchFilter filter
    ) {
        if (placeIds.isEmpty()) {
            return Map.of();
        }
        return lockerPlaceLockerReader.readByPlaceIds(
            latitude,
            longitude,
            placeIds,
            filter,
            currentRequestLanguage.resolve().languageTag()
        );
    }

    private LockerKeywordItemResult toKeywordItem(
        LockerSuggestItemResult item,
        Map<Long, List<LockerPlaceLocker>> placeLockersByPlaceId,
        Set<Long> favoriteLockerIds
    ) {
        if (item.type() == LockerItemType.LOCKER) {
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

    private Set<Long> collectTargetLockerIds(
        List<LockerSuggestItemResult> suggestItems,
        Map<Long, List<LockerPlaceLocker>> placeLockersByPlaceId
    ) {
        Set<Long> directLockerIds = suggestItems.stream()
            .filter(item -> item.type() == LockerItemType.LOCKER)
            .map(LockerSuggestItemResult::lockerId)
            .collect(Collectors.toSet());
        Set<Long> placeLockerIds = placeLockersByPlaceId.values().stream()
            .flatMap(List::stream)
            .map(LockerPlaceLocker::lockerId)
            .collect(Collectors.toSet());
        directLockerIds.addAll(placeLockerIds);
        return directLockerIds;
    }

    private Set<Long> resolveFavoriteLockerIds(Long userId, Set<Long> lockerIds) {
        if (userId == null || lockerIds.isEmpty()) {
            return Set.of();
        }
        return favoriteLockerReader.findFavoriteLockerIds(userId, lockerIds);
    }

    private void increaseKeywordCount(String keyword) {
        try {
            keywordCountCommandService.increase(keyword);
        } catch (DataAccessException exception) {
            log.warn("키워드 집계 저장에 실패해도 검색은 계속 진행합니다. keyword={}", keyword, exception);
        }
    }
}
