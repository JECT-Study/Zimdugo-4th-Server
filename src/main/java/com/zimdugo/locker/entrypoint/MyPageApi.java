package com.zimdugo.locker.entrypoint;

import com.zimdugo.common.security.CurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.locker.entrypoint.dto.request.report.LockerReportCreateRequest;
import com.zimdugo.locker.entrypoint.dto.response.mypage.MyLockerReportDetailResponse;
import com.zimdugo.locker.entrypoint.dto.response.mypage.MyLockerReportHistoryResponse;
import com.zimdugo.locker.entrypoint.dto.response.mypage.MyPageSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
        @Schema(minimum = "-90", maximum = "90")
        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0") Double latitude,
        @RequestParam(name = "lng", required = false)
        @Parameter(description = "사용자 경도", example = "127.027610")
        @Schema(minimum = "-180", maximum = "180")
        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0") Double longitude,
        @RequestParam(name = "page", defaultValue = "0")
        @Parameter(description = "페이지 번호", example = "0")
        @Schema(minimum = "0")
        @Min(0) int page,
        @RequestParam(name = "size", defaultValue = "20")
        @Parameter(description = "조회 개수", example = "20")
        @Schema(minimum = "1", maximum = "20")
        @Min(1)
        @Max(20) int size
    );

    @Operation(
        summary = "내 제보 단건 조회",
        description = "로그인한 사용자의 제보 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "내 제보 단건 조회 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "404", description = "제보를 찾을 수 없습니다.")
    })
    @GetMapping("/me/locker-reports/{reportId}")
    ResponseEntity<RestResponse<MyLockerReportDetailResponse>> getMyLockerReport(
        @CurrentUser Long userId,
        @PathVariable("reportId")
        @Parameter(description = "제보 ID", example = "10")
        @Positive
        Long reportId
    );

    @Operation(
        summary = "내 제보 수정",
        description = "로그인한 사용자의 제보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "내 제보 수정 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "404", description = "제보를 찾을 수 없습니다.")
    })
    @PatchMapping("/me/locker-reports/{reportId}")
    ResponseEntity<RestResponse<Void>> updateMyLockerReport(
        @CurrentUser Long userId,
        @PathVariable("reportId")
        @Parameter(description = "제보 ID", example = "10")
        @Positive
        Long reportId,
        @Valid @RequestBody LockerReportCreateRequest request
    );

    @Operation(
        summary = "내 제보 삭제",
        description = "로그인한 사용자의 제보를 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "내 제보 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
        @ApiResponse(responseCode = "404", description = "제보를 찾을 수 없습니다.")
    })
    @DeleteMapping("/me/locker-reports/{reportId}")
    ResponseEntity<RestResponse<Void>> deleteMyLockerReport(
        @CurrentUser Long userId,
        @PathVariable("reportId")
        @Parameter(description = "제보 ID", example = "10")
        @Positive
        Long reportId
    );
}
