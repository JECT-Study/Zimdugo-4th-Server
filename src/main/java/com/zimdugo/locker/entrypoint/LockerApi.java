package com.zimdugo.locker.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.locker.application.NearbyLockerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Locker", description = "보관함 조회 API")
public interface LockerApi {

    @Operation(
        summary = "주변 보관함 목록 조회",
        description = "현재 좌표 기준 반경 내 보관함 목록을 거리 오름차순으로 조회한다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @SecurityRequirements
    @GetMapping("/lockers/nearby")
    ResponseEntity<RestResponse<List<NearbyLockerResponse>>> getNearbyLockers(
        @RequestParam("lat")
        @Parameter(description = "사용자 위도", example = "37.498095")
        @Schema(minimum = "-90", maximum = "90")
        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0") double latitude,
        @RequestParam("lng")
        @Parameter(description = "사용자 경도", example = "127.027610")
        @Schema(minimum = "-180", maximum = "180")
        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0") double longitude,
        @RequestParam(name = "radius", defaultValue = "1000")
        @Parameter(description = "조회 반경(m), 기본값 1000, 최대 1000", example = "1000")
        @Schema(minimum = "1", maximum = "1000")
        @Min(1)
        @Max(1000) int radiusMeters
    );
}
