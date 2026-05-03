package com.zimdugo.locker.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.locker.application.LockerReportDuplicateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Locker Report", description = "보관함 제보 API")
public interface LockerReportApi {

    @Operation(
        summary = "보관함 제보 중복 후보 조회",
        description = "제보 등록 전 지정한 좌표 기준 30m 내 기존 보관함 후보를 조회한다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    @GetMapping("/locker-reports/duplicates")
    ResponseEntity<RestResponse<LockerReportDuplicateResponse>> findDuplicateLockerCandidates(
        Authentication authentication,
        @RequestParam("lat")
        @Parameter(description = "위도", example = "37.556")
        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0") double latitude,
        @RequestParam("lng")
        @Parameter(description = "경도", example = "126.923")
        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0") double longitude
    );

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
