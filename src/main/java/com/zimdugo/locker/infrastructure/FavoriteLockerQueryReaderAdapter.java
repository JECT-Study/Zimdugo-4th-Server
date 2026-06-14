package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.FavoriteLockerListItem;
import com.zimdugo.locker.domain.FavoriteLockerListPage;
import com.zimdugo.locker.domain.FavoriteLockerQueryReader;
import com.zimdugo.locker.infrastructure.localization.LocalizedLockerContent;
import com.zimdugo.locker.infrastructure.localization.TranslationLookupService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FavoriteLockerQueryReaderAdapter implements FavoriteLockerQueryReader {

    private final FavoriteLockerRepository favoriteLockerRepository;
    private final TranslationLookupService translationLookupService;

    @Override
    public FavoriteLockerListPage findAll(
        Long userId,
        double latitude,
        double longitude,
        int page,
        int size
    ) {
        Page<FavoriteLockerListQueryProjection> result = favoriteLockerRepository.findFavoriteLockers(
            userId,
            latitude,
            longitude,
            PageRequest.of(page, size)
        );

        Map<Long, LocalizedLockerContent> localizedLockers = localizedLockers(result.getContent());
        List<FavoriteLockerListItem> items = result.getContent().stream()
            .map(projection -> toDomain(projection, localizedLockers))
            .toList();

        return new FavoriteLockerListPage(items, result.getTotalElements(), result.hasNext());
    }

    private FavoriteLockerListItem toDomain(
        FavoriteLockerListQueryProjection projection,
        Map<Long, LocalizedLockerContent> localizedLockers
    ) {
        LocalizedLockerContent content = localizedLockers.get(projection.getLockerId());
        return new FavoriteLockerListItem(
            projection.getLockerId(),
            content == null ? projection.getLockerName() : content.name(),
            content == null ? projection.getRoadAddress() : content.roadAddress(),
            projection.getLockerType(),
            projection.getLatitude(),
            projection.getLongitude(),
            projection.getDistanceMeters(),
            projection.getUpdatedAt()
        );
    }

    private Map<Long, LocalizedLockerContent> localizedLockers(List<FavoriteLockerListQueryProjection> projections) {
        if (translationLookupService == null) {
            return Map.of();
        }
        return translationLookupService.resolveLockers(
            projections.stream().map(FavoriteLockerListQueryProjection::getLockerId).toList()
        );
    }
}
