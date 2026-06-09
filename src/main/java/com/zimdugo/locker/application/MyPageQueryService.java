package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.mypage.MyLockerReportHistoryItemResult;
import com.zimdugo.locker.application.result.mypage.MyLockerReportHistoryResult;
import com.zimdugo.locker.application.result.mypage.MyPageSummaryResult;
import com.zimdugo.locker.domain.MyLockerReportHistoryItem;
import com.zimdugo.locker.domain.MyLockerReportHistoryPage;
import com.zimdugo.locker.domain.MyPageReader;
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

    private MyLockerReportHistoryItemResult toLockerReportHistoryItemResult(MyLockerReportHistoryItem item) {
        return new MyLockerReportHistoryItemResult(
            item.reportId(),
            item.lockerName(),
            item.roadAddress(),
            item.lockerType(),
            item.latitude(),
            item.longitude(),
            item.distanceMeters(),
            item.updatedAt()
        );
    }
}
