package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.MyPageReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyPageReaderAdapter implements MyPageReader {

    private final FavoriteLockerRepository favoriteLockerRepository;
    private final LockerReportRepository lockerReportRepository;

    @Override
    public long countFavoriteLockers(Long userId) {
        return favoriteLockerRepository.countFavoriteLockersByUserId(userId);
    }

    @Override
    public long countLockerReports(Long userId) {
        return lockerReportRepository.countLockerReportsByUserId(userId);
    }
}
