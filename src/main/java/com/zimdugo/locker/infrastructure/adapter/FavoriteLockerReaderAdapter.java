package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.favorite.FavoriteLockerReader;
import com.zimdugo.locker.infrastructure.persistence.FavoriteLockerRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class FavoriteLockerReaderAdapter implements FavoriteLockerReader {

    private final FavoriteLockerRepository favoriteLockerRepository;

    @Override
    public boolean exists(Long userId, Long lockerId) {
        return favoriteLockerRepository.existsByUserIdAndLockerId(userId, lockerId);
    }

    @Override
    public Set<Long> findFavoriteLockerIds(Long userId, Set<Long> lockerIds) {
        if (lockerIds == null || lockerIds.isEmpty()) {
            return Set.of();
        }
        return favoriteLockerRepository.findFavoriteLockerIds(userId, lockerIds);
    }
}
