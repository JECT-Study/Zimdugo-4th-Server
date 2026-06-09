package com.zimdugo.locker.entrypoint.dto.response.mypage;

import com.zimdugo.locker.application.result.mypage.MyPageSummaryResult;

public record MyPageSummaryResponse(
    long favoriteLockerCount,
    long lockerReportCount
) {

    public static MyPageSummaryResponse from(MyPageSummaryResult result) {
        return new MyPageSummaryResponse(
            result.favoriteLockerCount(),
            result.lockerReportCount()
        );
    }
}
