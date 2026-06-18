package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.favorite.FavoriteLockerStore;
import com.zimdugo.locker.infrastructure.persistence.FavoriteLockerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class FavoriteLockerStoreAdapter implements FavoriteLockerStore {

    private final FavoriteLockerRepository favoriteLockerRepository;

    @Override
    public void save(Long userId, Long lockerId) {
        favoriteLockerRepository.insertIgnoreConflict(userId, lockerId);
    }

    @Override
    public void delete(Long userId, Long lockerId) {
        favoriteLockerRepository.deleteByUserIdAndLockerId(userId, lockerId);
    }
}
