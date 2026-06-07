package com.zimdugo.locker.domain;

public interface FavoriteLockerReader {
    boolean exists(Long userId, Long lockerId);
}
