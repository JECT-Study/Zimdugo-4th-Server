package com.zimdugo.locker.application.search;

import com.zimdugo.common.i18n.CurrentRequestLanguage;
import com.zimdugo.locker.application.pin.LockerSearchPinAssembler;
import com.zimdugo.locker.application.pin.LockerPinClusterer;
import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.application.result.search.LockerSearchItemResult;
import com.zimdugo.locker.application.result.search.LockerSearchLockerResult;
import com.zimdugo.locker.application.result.search.LockerSearchResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
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
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LockerSearchResultQueryService {

    private final LockerSearchQueryService lockerSearchQueryService;
    private final SearchKeywordCountCommandService searchKeywordCountCommandService;
    private final LockerPlaceLockerReader lockerPlaceLockerReader;
    private final LockerSearchPinAssembler lockerSearchPinAssembler;
    private final LockerPinClusterer lockerPinClusterer;
    private final FavoriteLockerReader favoriteLockerReader;
    private final CurrentRequestLanguage currentRequestLanguage;

    public LockerSearchResult getSearchResults(LockerSearchCommand command) {
        return getSearchResults(null, command);
    }

    public LockerSearchResult getSearchResults(Long userId, LockerSearchCommand command) {
        List<LockerSearchItemResult> items = getDisplayableItems(
            userId,
            command.latitude(),
            command.longitude(),
            command.keyword(),
            normalizeFilter(toSearchFilter(command))
        );
        return LockerSearchResult.of(items);
    }

    public LockerSearchResult getSearchResultsWithPins(LockerSearchCommand command) {
        return getSearchResultsWithPins(null, command);
    }

    public LockerSearchResult getSearchResultsWithPins(Long userId, LockerSearchCommand command) {
        List<LockerSearchItemResult> items = getDisplayableItems(
            userId,
            command.latitude(),
            command.longitude(),
            command.keyword(),
            normalizeFilter(toSearchFilter(command))
        );
        if (items.isEmpty()) {
            return LockerSearchResult.empty();
        }
        List<com.zimdugo.locker.application.result.pin.LockerPinItemResult> pins =
            lockerSearchPinAssembler.assemble(items);
        if (command.zoom() != null) {
            pins = lockerPinClusterer.cluster(pins, command.zoom());
        }
        return LockerSearchResult.of(items, pins);
    }

    public LockerSearchResult getSearchResults(
        double latitude,
        double longitude,
        String keyword,
        LockerSearchFilter filter
    ) {
        return getSearchResults(null, latitude, longitude, keyword, filter);
    }

    public LockerSearchResult getSearchResults(
        Long userId,
        double latitude,
        double longitude,
        String keyword,
        LockerSearchFilter filter
    ) {
        List<LockerSearchItemResult> items = getDisplayableItems(
            userId,
            latitude,
            longitude,
            keyword,
            normalizeFilter(filter)
        );
        return LockerSearchResult.of(items);
    }

    private LockerSearchFilter toSearchFilter(LockerSearchCommand command) {
        return LockerSearchFilter.from(
            command.sizeTypes(),
            command.indoorOutdoorTypes(),
            command.lockerTypes()
        );
    }

    private LockerSearchFilter normalizeFilter(LockerSearchFilter filter) {
        return filter == null ? LockerSearchFilter.empty() : filter;
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

    private LockerSearchItemResult toSearchItem(
        LockerSuggestItemResult item,
        Map<Long, List<LockerPlaceLocker>> placeLockersByPlaceId,
        Set<Long> favoriteLockerIds
    ) {
        if (item.type() == LockerItemType.LOCKER) {
            return LockerSearchItemResult.locker(item, favoriteLockerIds.contains(item.lockerId()));
        }

        List<LockerSearchLockerResult> lockers = placeLockersByPlaceId
            .getOrDefault(item.placeId(), List.of())
            .stream()
            .map(locker -> LockerSearchLockerResult.from(
                locker,
                favoriteLockerIds.contains(locker.lockerId())
            ))
            .toList();
        return LockerSearchItemResult.place(item, lockers);
    }

    private boolean hasDisplayableResult(LockerSearchItemResult item) {
        return item.type() == LockerItemType.LOCKER || !item.lockers().isEmpty();
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

    private void increaseSearchKeywordCount(String keyword) {
        try {
            searchKeywordCountCommandService.increase(keyword);
        } catch (DataAccessException | TransactionException exception) {
            log.warn("키워드 집계 저장에 실패해도 검색은 계속 진행합니다. keyword={}", keyword, exception);
        }
    }

    public List<LockerSearchItemResult> getDisplayableSearchItemsForPins(
        Long userId,
        double latitude,
        double longitude,
        String keyword,
        LockerSearchFilter filter
    ) {
        return getDisplayableItemsWithoutCounting(userId, latitude, longitude, keyword, filter);
    }

    private List<LockerSearchItemResult> getDisplayableItems(
        Long userId,
        double latitude,
        double longitude,
        String keyword,
        LockerSearchFilter filter
    ) {
        increaseSearchKeywordCount(keyword);
        return getDisplayableItemsWithoutCounting(userId, latitude, longitude, keyword, filter);
    }

    private List<LockerSearchItemResult> getDisplayableItemsWithoutCounting(
        Long userId,
        double latitude,
        double longitude,
        String keyword,
        LockerSearchFilter filter
    ) {
        filter = normalizeFilter(filter);
        List<LockerSuggestItemResult> suggestItems = lockerSearchQueryService.search(
            latitude,
            longitude,
            keyword,
            filter
        );
        if (suggestItems.isEmpty()) {
            return List.of();
        }

        List<Long> placeIds = suggestItems.stream()
            .filter(item -> item.type() == LockerItemType.PLACE)
            .map(LockerSuggestItemResult::placeId)
            .toList();
        Map<Long, List<LockerPlaceLocker>> placeLockersByPlaceId =
            readPlaceLockers(latitude, longitude, placeIds, filter);
        Set<Long> targetLockerIds = collectTargetLockerIds(suggestItems, placeLockersByPlaceId);
        Set<Long> favoriteLockerIds = resolveFavoriteLockerIds(userId, targetLockerIds);

        return suggestItems.stream()
            .map(item -> toSearchItem(item, placeLockersByPlaceId, favoriteLockerIds))
            .filter(this::hasDisplayableResult)
            .toList();
    }
}
