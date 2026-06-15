package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.MyPageReader;
import com.zimdugo.locker.domain.MyLockerReportDetail;
import com.zimdugo.locker.domain.MyLockerReportHistoryItem;
import com.zimdugo.locker.domain.MyLockerReportHistoryPage;
import java.util.List;
import java.util.Optional;
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

    @Override
    public Optional<MyLockerReportDetail> findLockerReport(Long userId, Long reportId) {
        return lockerReportRepository.findActiveByIdAndUserId(reportId, userId)
            .map(report -> new MyLockerReportDetail(
                report.getId(),
                report.getName(),
                report.getRoadAddress(),
                report.getLatitude(),
                report.getLongitude(),
                report.getGroundLevelType() != null && report.getFloor() != null,
                report.getGroundLevelType() == null ? null : report.getGroundLevelType().name(),
                report.getFloor(),
                report.getIndoorOutdoorType().name(),
                report.getLockerType().name(),
                report.getLockerSize().stream()
                    .map(Enum::name)
                    .sorted()
                    .toList(),
                report.getIsFree(),
                report.getMinPrice(),
                report.getMaxPrice(),
                report.is24Hours(),
                report.getStartTime(),
                report.getEndTime(),
                report.getAdditionalInfo(),
                report.getImageUrl(),
                report.isLocationConsentAgreed()
            ));
    }

    private MyLockerReportHistoryItem toLockerReportHistoryItem(
        MyLockerReportHistoryQueryProjection projection
    ) {
        return new MyLockerReportHistoryItem(
            projection.getReportId(),
            projection.getLockerName(),
            projection.getRoadAddress(),
            projection.getLockerType(),
            projection.getImageUrl(),
            projection.getLatitude(),
            projection.getLongitude(),
            projection.getDistanceMeters(),
            projection.getUpdatedAt()
        );
    }
}
