package com.zimdugo.locker.entrypoint;

import com.zimdugo.common.security.CurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.locker.entrypoint.dto.request.report.LockerReportCreateRequest;
import com.zimdugo.locker.entrypoint.dto.request.report.LockerReportNamePreviewRequest;
import com.zimdugo.locker.entrypoint.dto.response.report.LockerReportCreateResponse;
import com.zimdugo.locker.entrypoint.dto.response.report.LockerReportNamePreviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Locker Report", description = "보관함 제보 API")
public interface LockerReportApi {

    @Operation(
        summary = "제보 이름 미리보기",
        description = "지도에서 선택한 위치를 기준으로 즉시 표시할 제보 이름을 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "미리보기 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @SecurityRequirements
    @GetMapping("/locker-reports/name-preview")
    ResponseEntity<RestResponse<LockerReportNamePreviewResponse>> previewLockerReportName(
        @Valid @ParameterObject LockerReportNamePreviewRequest request
    );

    @Operation(
        summary = "보관함 제보 생성",
        description = "로그인한 사용자의 보관함 제보를 생성합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "필수값 누락 또는 잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    @PostMapping("/locker-reports")
    ResponseEntity<RestResponse<LockerReportCreateResponse>> createLockerReport(
        @CurrentUser Long userId,
        @Valid @RequestBody LockerReportCreateRequest request
    );
}
