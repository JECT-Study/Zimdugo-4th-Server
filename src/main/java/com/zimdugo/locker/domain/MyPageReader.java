package com.zimdugo.locker.domain;

public interface MyPageReader {

    long countFavoriteLockers(Long userId);

    long countLockerReports(Long userId);
}
