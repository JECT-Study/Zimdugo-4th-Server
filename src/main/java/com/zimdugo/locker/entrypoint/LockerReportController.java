package com.zimdugo.locker.entrypoint;

import com.zimdugo.common.security.CurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.report.LockerReportCommandService;
import com.zimdugo.locker.application.result.report.LockerReportCreateResult;
import com.zimdugo.locker.entrypoint.dto.request.report.LockerReportCreateRequest;
import com.zimdugo.locker.entrypoint.dto.response.report.LockerReportCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LockerReportController implements LockerReportApi {

    private final LockerReportCommandService lockerReportCommandService;

    @Override
    public ResponseEntity<RestResponse<LockerReportCreateResponse>> createLockerReport(
        @CurrentUser Long userId,
        LockerReportCreateRequest request
    ) {
        LockerReportCreateResult result = lockerReportCommandService.create(
            userId,
            request.toCommand()
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, toResponse(result)));
    }

    private LockerReportCreateResponse toResponse(LockerReportCreateResult result) {
        return new LockerReportCreateResponse(
            result.reportId(),
            result.roadAddress(),
            result.latitude(),
            result.longitude(),
            result.reportStatus()
        );
    }
}
