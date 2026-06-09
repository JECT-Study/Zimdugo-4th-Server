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

    static final double DEFAULT_LATITUDE = 37.498095;
    static final double DEFAULT_LONGITUDE = 127.027610;

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
        validateLocation(latitude, longitude);

        double resolvedLatitude = latitude == null ? DEFAULT_LATITUDE : latitude;
        double resolvedLongitude = longitude == null ? DEFAULT_LONGITUDE : longitude;

        MyLockerReportHistoryPage result = myPageReader.findLockerReports(
            userId,
            resolvedLatitude,
            resolvedLongitude,
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

    private void validateLocation(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
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
