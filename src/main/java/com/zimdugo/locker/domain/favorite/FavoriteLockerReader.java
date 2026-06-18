package com.zimdugo.locker.domain.favorite;

import java.util.Set;

public interface FavoriteLockerReader {
    boolean exists(Long userId, Long lockerId);
    Set<Long> findFavoriteLockerIds(Long userId, Set<Long> lockerIds);
}
