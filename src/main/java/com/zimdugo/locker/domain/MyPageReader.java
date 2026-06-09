package com.zimdugo.locker.domain;

public interface MyPageReader {

    long countFavoriteLockers(Long userId);

    long countLockerReports(Long userId);

    MyLockerReportHistoryPage findLockerReports(
        Long userId,
        double latitude,
        double longitude,
        int page,
        int size
    );
}
