package com.zimdugo.locker.entrypoint;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.LockerReportCommandService;
import com.zimdugo.locker.application.LockerReportCreateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
        Authentication authentication,
        LockerReportCreateRequest request
    ) {
        LockerReportCreateResult result = lockerReportCommandService.create(
            extractUserId(authentication),
            request.toCommand()
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, toResponse(result)));
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATED_USER_NOT_FOUND);
        }

        return Long.valueOf(authentication.getName());
    }

    private LockerReportCreateResponse toResponse(LockerReportCreateResult result) {
        return new LockerReportCreateResponse(
            result.reportId(),
            result.lockerId(),
            result.name(),
            result.roadAddress(),
            result.latitude(),
            result.longitude(),
            result.reportStatus()
        );
    }
}
