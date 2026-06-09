package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.MyPageReader;
import com.zimdugo.locker.domain.MyLockerReportHistoryItem;
import com.zimdugo.locker.domain.MyLockerReportHistoryPage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @Override
    public MyLockerReportHistoryPage findLockerReports(
        Long userId,
        double latitude,
        double longitude,
        int page,
        int size
    ) {
        Page<MyLockerReportHistoryQueryProjection> result = lockerReportRepository.findMyLockerReports(
            userId,
            latitude,
            longitude,
            PageRequest.of(page, size)
        );

        List<MyLockerReportHistoryItem> items = result.getContent().stream()
            .map(this::toLockerReportHistoryItem)
            .toList();

        return new MyLockerReportHistoryPage(items, result.getTotalElements(), result.hasNext());
    }

    private MyLockerReportHistoryItem toLockerReportHistoryItem(
        MyLockerReportHistoryQueryProjection projection
    ) {
        return new MyLockerReportHistoryItem(
            projection.getReportId(),
            projection.getLockerName(),
            projection.getRoadAddress(),
            projection.getLockerType(),
            projection.getLatitude(),
            projection.getLongitude(),
            projection.getDistanceMeters(),
            projection.getUpdatedAt()
        );
    }
}
