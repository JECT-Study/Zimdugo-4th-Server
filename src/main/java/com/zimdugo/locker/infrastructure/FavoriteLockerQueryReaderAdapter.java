package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.FavoriteLockerListItem;
import com.zimdugo.locker.domain.FavoriteLockerListPage;
import com.zimdugo.locker.domain.FavoriteLockerQueryReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FavoriteLockerQueryReaderAdapter implements FavoriteLockerQueryReader {

    private final FavoriteLockerRepository favoriteLockerRepository;

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

        List<FavoriteLockerListItem> items = result.getContent().stream()
            .map(this::toDomain)
            .toList();

        return new FavoriteLockerListPage(items, result.getTotalElements(), result.hasNext());
    }

    private FavoriteLockerListItem toDomain(FavoriteLockerListQueryProjection projection) {
        return new FavoriteLockerListItem(
            projection.getLockerId(),
            projection.getLockerName(),
            projection.getRoadAddress(),
            projection.getLockerType(),
            projection.getLatitude(),
            projection.getLongitude(),
            projection.getDistanceMeters(),
            projection.getUpdatedAt()
        );
    }
}
