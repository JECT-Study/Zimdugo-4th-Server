package com.zimdugo.locker.entrypoint;

import com.zimdugo.core.response.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Locker Report", description = "보관함 제보 API")
public interface LockerReportApi {

    @Operation(
        summary = "신규 보관함 제보 등록",
        description = "로그인 사용자의 보관함 제보를 등록한다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "등록 성공"),
        @ApiResponse(responseCode = "400", description = "필수값 누락 또는 잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    @PostMapping("/locker-reports")
    ResponseEntity<RestResponse<LockerReportCreateResponse>> createLockerReport(
        Authentication authentication,
        @Valid @RequestBody LockerReportCreateRequest request
    );
}
