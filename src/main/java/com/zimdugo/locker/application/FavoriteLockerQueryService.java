package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.FavoriteLocker;
import com.zimdugo.locker.domain.FavoriteLockerPage;
import com.zimdugo.locker.domain.FavoriteLockerReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteLockerQueryService {

    private final FavoriteLockerReader favoriteLockerReader;

    @Transactional(readOnly = true)
    public FavoriteLockerResponse getFavorites(Long userId, int page, int size, Double latitude, Double longitude) {
        FavoriteLockerPage favoritePage = favoriteLockerReader.findByUserId(userId, page, size, latitude, longitude);

        return new FavoriteLockerResponse(
            favoritePage.totalCount(),
            favoritePage.page(),
            favoritePage.size(),
            favoritePage.hasNext(),
            favoritePage.favorites().stream()
                .map(this::toItemResponse)
                .toList()
        );
    }

    @Transactional(readOnly = true)
    public FavoriteLockerStatusResponse getFavoriteStatus(Long userId, Long lockerId) {
        return new FavoriteLockerStatusResponse(
            lockerId,
            favoriteLockerReader.existsByUserIdAndLockerId(userId, lockerId)
        );
    }

    private FavoriteLockerItemResponse toItemResponse(FavoriteLocker favorite) {
        return new FavoriteLockerItemResponse(
            favorite.lockerId(),
            favorite.poiName(),
            favorite.roadAddress(),
            favorite.latitude(),
            favorite.longitude(),
            favorite.favoritedAt(),
            favorite.lastCompletedVoteAt(),
            favorite.distanceMeters()
        );
    }
}
