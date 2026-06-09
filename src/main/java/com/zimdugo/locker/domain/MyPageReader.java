package com.zimdugo.locker.domain;

import java.util.Optional;

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

    Optional<MyLockerReportDetail> findLockerReport(Long userId, Long reportId);
}
