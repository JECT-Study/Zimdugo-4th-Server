package com.zimdugo.locker.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.locker.entrypoint.dto.response.keyword.LockerKeywordResponse;
import com.zimdugo.locker.entrypoint.dto.response.pin.LockerPinResponse;
import com.zimdugo.locker.entrypoint.dto.response.suggest.LockerSuggestResponse;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Locker", description = "보관함 조회 API")
public interface LockerApi {

    @Operation(
        summary = "지도 핀 조회",
        description = "현재 좌표 기준 반경 내 핀 목록을 반환한다. 같은 장소의 보관함이 1개면 LOCKER, 2개 이상이면 PLACE 핀으로 반환한다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @SecurityRequirements
    @GetMapping("/lockers/pin")
    ResponseEntity<RestResponse<LockerPinResponse>> getPins(
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
        @RequestParam(name = "radius", defaultValue = "500")
        @Parameter(description = "조회 반경(m)", example = "500")
        @Schema(minimum = "1", maximum = "7000")
        @Min(1)
        @Max(7000) int radiusMeters
    );

    @Operation(
        summary = "보관함 자동완성 조회",
        description = "현재 좌표 기준 거리순 장소/보관함 자동완성 목록을 반환한다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @SecurityRequirements
    @GetMapping("/lockers/suggest")
    ResponseEntity<RestResponse<LockerSuggestResponse>> getSuggestions(
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
        @RequestParam("keyword")
        @Parameter(description = "자동완성 키워드", example = "신촌역 1번 출구 b")
        @NotBlank
        @Size(max = 100) String keyword,
        @RequestParam(name = "limit", defaultValue = "10")
        @Parameter(description = "최대 결과 개수, 기본값 10, 최대 20", example = "10")
        @Schema(minimum = "1", maximum = "20")
        @Min(1)
        @Max(20) int limit
    );

    @Operation(
        summary = "보관함 키워드 검색 조회",
        description = "현재 좌표 기준 거리순 장소/보관함 검색 결과를 반환한다. PLACE 결과는 하위 보관함 목록을 함께 반환한다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @SecurityRequirements
    @GetMapping("/lockers/keyword")
    ResponseEntity<RestResponse<LockerKeywordResponse>> getKeywordResults(
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
        @RequestParam("keyword")
        @Parameter(description = "검색 키워드", example = "신촌역 1번 출구")
        @NotBlank
        @Size(max = 100) String keyword,
        @RequestParam(name = "limit", defaultValue = "10")
        @Parameter(description = "최대 결과 개수, 기본값 10, 최대 20", example = "10")
        @Schema(minimum = "1", maximum = "20")
        @Min(1)
        @Max(20) int limit
    );
}
