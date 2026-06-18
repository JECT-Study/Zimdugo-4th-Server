package com.zimdugo.locker.application.mypage;

import com.zimdugo.locker.application.common.ActiveUserValidator;
import com.zimdugo.locker.application.common.UserLocationResolver;

import com.zimdugo.locker.application.result.mypage.MyLockerReportHistoryItemResult;
import com.zimdugo.locker.application.result.mypage.MyLockerReportDetailResult;
import com.zimdugo.locker.application.result.mypage.MyLockerReportHistoryResult;
import com.zimdugo.locker.application.result.mypage.MyPageSummaryResult;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.mypage.MyLockerReportDetail;
import com.zimdugo.locker.domain.mypage.MyLockerReportHistoryItem;
import com.zimdugo.locker.domain.mypage.MyLockerReportHistoryPage;
import com.zimdugo.locker.domain.mypage.MyPageReader;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageQueryService {

    private final MyPageReader myPageReader;
    private final ActiveUserValidator activeUserValidator;

    public MyPageSummaryResult getSummary(Long userId) {
        activeUserValidator.validate(userId);

        return new MyPageSummaryResult(
            myPageReader.countFavoriteLockers(userId),
            myPageReader.countLockerReports(userId)
        );
    }

    public MyLockerReportHistoryResult getLockerReports(
        Long userId,
        Double latitude,
        Double longitude,
        int page,
        int size
    ) {
        activeUserValidator.validate(userId);
        UserLocationResolver.ResolvedLocation resolvedLocation = UserLocationResolver.resolve(latitude, longitude);

        MyLockerReportHistoryPage result = myPageReader.findLockerReports(
            userId,
            resolvedLocation.latitude(),
            resolvedLocation.longitude(),
            page,
            size
        );

        if (result.items().isEmpty()) {
            return MyLockerReportHistoryResult.of(
                Collections.emptyList(),
                result.totalCount(),
                result.hasNext()
            );
        }

        List<MyLockerReportHistoryItemResult> items = result.items().stream()
            .map(this::toLockerReportHistoryItemResult)
            .toList();

        return MyLockerReportHistoryResult.of(items, result.totalCount(), result.hasNext());
    }

    public MyLockerReportDetailResult getLockerReport(Long userId, Long reportId) {
        activeUserValidator.validate(userId);

        MyLockerReportDetail report = myPageReader.findLockerReport(userId, reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_REPORT_NOT_FOUND));

        return new MyLockerReportDetailResult(
            report.reportId(),
            report.lockerName(),
            report.roadAddress(),
            report.latitude(),
            report.longitude(),
            report.hasFloor(),
            report.floorType(),
            report.floorNumber(),
            report.indoorOutdoorType(),
            report.lockerType(),
            report.sizeTypes(),
            report.priceType(),
            report.minPrice(),
            report.maxPrice(),
            report.operatingTimeType(),
            report.startTime(),
            report.endTime(),
            report.additionalInfo(),
            report.imageUrl(),
            report.locationConsentAgreed()
        );
    }

    private MyLockerReportHistoryItemResult toLockerReportHistoryItemResult(MyLockerReportHistoryItem item) {
        return new MyLockerReportHistoryItemResult(
            item.reportId(),
            item.lockerName(),
            item.roadAddress(),
            item.lockerType(),
            item.imageUrl(),
            item.latitude(),
            item.longitude(),
            item.distanceMeters(),
            item.updatedAt()
        );
    }
}
