package com.zimdugo.locker.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.locker.entrypoint.dto.response.favorite.FavoriteLockerListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Locker Favorite", description = "보관함 즐겨찾기 API")
public interface LockerFavoriteApi {

    @Operation(
        summary = "보관함 즐겨찾기 등록",
        description = "로그인 사용자의 즐겨찾기 보관함으로 등록합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "등록 성공"),
        @ApiResponse(responseCode = "401", description = "로그인 필요"),
        @ApiResponse(responseCode = "404", description = "보관함이 존재하지 않음")
    })
    @PostMapping("/me/favorite-lockers/{lockerId}")
    ResponseEntity<RestResponse<Void>> addFavoriteLocker(
        Authentication authentication,
        @PathVariable("lockerId")
        @Parameter(description = "보관함 ID", example = "10")
        @Positive
        Long lockerId
    );

    @Operation(
        summary = "보관함 즐겨찾기 해제",
        description = "로그인 사용자의 즐겨찾기 보관함에서 제거합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "해제 성공"),
        @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    @DeleteMapping("/me/favorite-lockers/{lockerId}")
    ResponseEntity<RestResponse<Void>> removeFavoriteLocker(
        Authentication authentication,
        @PathVariable("lockerId")
        @Parameter(description = "보관함 ID", example = "10")
        @Positive
        Long lockerId
    );

    @Operation(
        summary = "보관함 즐겨찾기 목록 조회",
        description = "로그인 사용자의 즐겨찾기 보관함 목록을 조회합니다. 위치 정보가 없으면 기본 위치 기준 거리를 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    @GetMapping("/me/favorite-lockers")
    ResponseEntity<RestResponse<FavoriteLockerListResponse>> getFavoriteLockers(
        Authentication authentication,
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
}
