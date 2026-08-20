package com.zimdugo.locker.application.search;

import com.zimdugo.common.i18n.CurrentRequestLanguage;
import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.application.result.search.LockerSearchItemResult;
import com.zimdugo.locker.application.result.search.LockerSearchLockerResult;
import com.zimdugo.locker.domain.favorite.FavoriteLockerReader;
import com.zimdugo.locker.domain.place.LockerPlaceLocker;
import com.zimdugo.locker.domain.place.LockerPlaceLockerReader;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LockerSearchDisplayQueryService {

    private final LockerSearchTargetQueryService lockerSearchTargetQueryService;
    private final LockerPlaceLockerReader lockerPlaceLockerReader;
    private final FavoriteLockerReader favoriteLockerReader;
    private final CurrentRequestLanguage currentRequestLanguage;
    private final int resultLimit;

    public LockerSearchDisplayQueryService(
        LockerSearchTargetQueryService lockerSearchTargetQueryService,
        LockerPlaceLockerReader lockerPlaceLockerReader,
        FavoriteLockerReader favoriteLockerReader,
        CurrentRequestLanguage currentRequestLanguage,
        @Value("${search.result-limit}") int resultLimit
    ) {
        this.lockerSearchTargetQueryService = lockerSearchTargetQueryService;
        this.lockerPlaceLockerReader = lockerPlaceLockerReader;
        this.favoriteLockerReader = favoriteLockerReader;
        this.currentRequestLanguage = currentRequestLanguage;
        this.resultLimit = resultLimit;
    }

    public List<LockerSearchItemResult> getDisplayableItems(
        Long userId,
        double latitude,
        double longitude,
        String keyword,
        LockerSearchFilter filter
    ) {
        LockerSearchFilter effectiveFilter = filter == null ? LockerSearchFilter.empty() : filter;
        List<LockerSearchTarget> targets = lockerSearchTargetQueryService.findTargets(
            latitude,
            longitude,
            keyword,
            effectiveFilter,
            resultLimit
        );
        if (targets.isEmpty()) {
            return List.of();
        }

        Map<Long, List<LockerPlaceLocker>> placeLockersByPlaceId = readPlaceLockers(
            latitude,
            longitude,
            collectPlaceIds(targets),
            effectiveFilter
        );
        Set<Long> favoriteLockerIds = resolveFavoriteLockerIds(
            userId,
            collectLockerIds(targets, placeLockersByPlaceId)
        );

        return targets.stream()
            .map(target -> toSearchItem(target, placeLockersByPlaceId, favoriteLockerIds))
            .filter(this::hasDisplayableResult)
            .toList();
    }

    private List<Long> collectPlaceIds(List<LockerSearchTarget> targets) {
        return targets.stream()
            .filter(target -> target.type() == LockerItemType.PLACE)
            .map(LockerSearchTarget::placeId)
            .toList();
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

    private Set<Long> collectLockerIds(
        List<LockerSearchTarget> targets,
        Map<Long, List<LockerPlaceLocker>> placeLockersByPlaceId
    ) {
        Set<Long> lockerIds = targets.stream()
            .filter(target -> target.type() == LockerItemType.LOCKER)
            .map(LockerSearchTarget::lockerId)
            .collect(Collectors.toSet());
        placeLockersByPlaceId.values().stream()
            .flatMap(List::stream)
            .map(LockerPlaceLocker::lockerId)
            .forEach(lockerIds::add);
        return lockerIds;
    }

    private Set<Long> resolveFavoriteLockerIds(Long userId, Set<Long> lockerIds) {
        if (userId == null || lockerIds.isEmpty()) {
            return Set.of();
        }
        return favoriteLockerReader.findFavoriteLockerIds(userId, lockerIds);
    }

    private LockerSearchItemResult toSearchItem(
        LockerSearchTarget target,
        Map<Long, List<LockerPlaceLocker>> placeLockersByPlaceId,
        Set<Long> favoriteLockerIds
    ) {
        if (target.type() == LockerItemType.LOCKER) {
            return LockerSearchItemResult.locker(target, favoriteLockerIds.contains(target.lockerId()));
        }

        List<LockerSearchLockerResult> lockers = placeLockersByPlaceId
            .getOrDefault(target.placeId(), List.of())
            .stream()
            .map(locker -> LockerSearchLockerResult.from(
                locker,
                favoriteLockerIds.contains(locker.lockerId())
            ))
            .toList();
        return LockerSearchItemResult.place(target, lockers);
    }

    private boolean hasDisplayableResult(LockerSearchItemResult item) {
        return item.type() == LockerItemType.LOCKER || !item.lockers().isEmpty();
    }
}
