package com.zimdugo.locker.domain;

public interface FavoriteLockerReader {

    FavoriteLockerPage findByUserId(Long userId, int page, int size, Double latitude, Double longitude);

    boolean existsByUserIdAndLockerId(Long userId, Long lockerId);
}
