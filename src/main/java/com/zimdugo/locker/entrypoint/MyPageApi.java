package com.zimdugo.locker.entrypoint;

import com.zimdugo.common.security.CurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.locker.entrypoint.dto.response.mypage.MyLockerReportHistoryResponse;
import com.zimdugo.locker.entrypoint.dto.response.mypage.MyPageSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "My Page", description = "마이페이지 요약 정보 조회 API")
public interface MyPageApi {

    @Operation(
        summary = "마이페이지 요약 정보 조회",
        description = "로그인한 사용자의 즐겨찾기 개수와 제보 히스토리 개수를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "마이페이지 요약 정보 조회 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다.")
    })
    @GetMapping("/me/summary")
    ResponseEntity<RestResponse<MyPageSummaryResponse>> getMyPageSummary(
        @CurrentUser Long userId
    );

    @Operation(
        summary = "내 제보 히스토리 조회",
        description = "로그인한 사용자의 제보 히스토리 목록을 최신 제보 순으로 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "내 제보 히스토리 조회 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다.")
    })
    @GetMapping("/me/locker-reports")
    ResponseEntity<RestResponse<MyLockerReportHistoryResponse>> getMyLockerReports(
        @CurrentUser Long userId,
        @RequestParam(name = "lat", required = false)
        @Parameter(description = "사용자 위도", example = "37.498095")
        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0") Double latitude,
        @RequestParam(name = "lng", required = false)
        @Parameter(description = "사용자 경도", example = "127.027610")
        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0") Double longitude,
        @RequestParam(name = "page", defaultValue = "0")
        @Parameter(description = "페이지 번호", example = "0")
        @Min(0) int page,
        @RequestParam(name = "size", defaultValue = "20")
        @Parameter(description = "조회 개수", example = "20")
        @Min(1)
        @Max(20) int size
    );
}
