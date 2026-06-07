package com.zimdugo.locker.domain;

public interface FavoriteLockerQueryReader {
    FavoriteLockerListPage findAll(Long userId, double latitude, double longitude, int page, int size);
}
