package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.result.mypage.MyLockerReportHistoryItemResult;
import com.zimdugo.locker.application.result.mypage.MyLockerReportHistoryResult;
import com.zimdugo.locker.application.result.mypage.MyPageSummaryResult;
import com.zimdugo.locker.domain.MyLockerReportHistoryItem;
import com.zimdugo.locker.domain.MyLockerReportHistoryPage;
import com.zimdugo.locker.domain.MyPageReader;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserReader;
import com.zimdugo.user.domain.UserStatus;
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
    private final UserReader userReader;

    public MyPageSummaryResult getSummary(Long userId) {
        validateUser(userId);

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
        validateUser(userId);
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

    private void validateUser(Long userId) {
        User user = userReader.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException(ErrorCode.USER_ALREADY_WITHDRAWN);
        }
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
