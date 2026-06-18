package com.zimdugo.locker.domain.favorite;

public interface FavoriteLockerStore {
    void save(Long userId, Long lockerId);

    void delete(Long userId, Long lockerId);
}
