package com.zimdugo.locker.entrypoint;

import com.zimdugo.common.security.CurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.result.mypage.MyLockerReportHistoryResult;
import com.zimdugo.locker.application.MyPageQueryService;
import com.zimdugo.locker.application.result.mypage.MyPageSummaryResult;
import com.zimdugo.locker.entrypoint.dto.response.mypage.MyLockerReportHistoryResponse;
import com.zimdugo.locker.entrypoint.dto.response.mypage.MyPageSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MyPageController implements MyPageApi {

    private final MyPageQueryService myPageQueryService;

    @Override
    public ResponseEntity<RestResponse<MyPageSummaryResponse>> getMyPageSummary(
        @CurrentUser Long userId
    ) {
        MyPageSummaryResult result = myPageQueryService.getSummary(userId);
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, MyPageSummaryResponse.from(result)));
    }

    @Override
    public ResponseEntity<RestResponse<MyLockerReportHistoryResponse>> getMyLockerReports(
        @CurrentUser Long userId,
        Double latitude,
        Double longitude,
        int page,
        int size
    ) {
        MyLockerReportHistoryResult result = myPageQueryService.getLockerReports(
            userId,
            latitude,
            longitude,
            page,
            size
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, MyLockerReportHistoryResponse.from(result)));
    }
}
