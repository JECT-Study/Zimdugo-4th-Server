package com.zimdugo.locker.entrypoint;

import com.zimdugo.common.security.CurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.LockerReportCommandService;
import com.zimdugo.locker.application.result.mypage.MyLockerReportDetailResult;
import com.zimdugo.locker.application.result.mypage.MyLockerReportHistoryResult;
import com.zimdugo.locker.application.MyPageQueryService;
import com.zimdugo.locker.application.result.mypage.MyPageSummaryResult;
import com.zimdugo.locker.entrypoint.dto.request.report.LockerReportCreateRequest;
import com.zimdugo.locker.entrypoint.dto.response.mypage.MyLockerReportDetailResponse;
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
    private final LockerReportCommandService lockerReportCommandService;

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

    @Override
    public ResponseEntity<RestResponse<MyLockerReportDetailResponse>> getMyLockerReport(
        @CurrentUser Long userId,
        Long reportId
    ) {
        MyLockerReportDetailResult result = myPageQueryService.getLockerReport(userId, reportId);
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, MyLockerReportDetailResponse.from(result)));
    }

    @Override
    public ResponseEntity<RestResponse<Void>> updateMyLockerReport(
        @CurrentUser Long userId,
        Long reportId,
        LockerReportCreateRequest request
    ) {
        lockerReportCommandService.update(userId, reportId, request.toCommand());
        return ResponseEntity.ok(RestResponse.ok(SuccessCode.OK));
    }

    @Override
    public ResponseEntity<RestResponse<Void>> deleteMyLockerReport(
        @CurrentUser Long userId,
        Long reportId
    ) {
        lockerReportCommandService.delete(userId, reportId);
        return ResponseEntity.ok(RestResponse.ok(SuccessCode.OK));
    }
}
