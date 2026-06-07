package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.FavoriteLockerReader;
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
        if (lockerIds.isEmpty()) {
            return Set.of();
        }
        return favoriteLockerRepository.findFavoriteLockerIds(userId, lockerIds);
    }
}
