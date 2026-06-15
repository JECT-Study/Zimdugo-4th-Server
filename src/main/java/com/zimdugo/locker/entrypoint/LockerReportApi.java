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

@Tag(name = "Locker Report", description = "Locker report API")
public interface LockerReportApi {

    @Operation(
        summary = "Locker report name preview",
        description = "Returns the report name to show immediately for the selected map pin."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Preview success"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameter")
    })
    @SecurityRequirements
    @GetMapping("/locker-reports/name-preview")
    ResponseEntity<RestResponse<LockerReportNamePreviewResponse>> previewLockerReportName(
        @Valid @ParameterObject LockerReportNamePreviewRequest request
    );

    @Operation(
        summary = "Create locker report",
        description = "Creates a locker report for the logged-in user."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Create success"),
        @ApiResponse(responseCode = "400", description = "Missing required value or invalid request"),
        @ApiResponse(responseCode = "401", description = "Login required")
    })
    @PostMapping("/locker-reports")
    ResponseEntity<RestResponse<LockerReportCreateResponse>> createLockerReport(
        @CurrentUser Long userId,
        @Valid @RequestBody LockerReportCreateRequest request
    );
}
