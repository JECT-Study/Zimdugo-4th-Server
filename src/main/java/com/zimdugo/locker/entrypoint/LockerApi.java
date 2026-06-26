package com.zimdugo.locker.entrypoint;

import com.zimdugo.common.security.NullableCurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.locker.entrypoint.dto.request.keyword.LockerKeywordRequest;
import com.zimdugo.locker.entrypoint.dto.request.place.PlaceLockerRequest;
import com.zimdugo.locker.entrypoint.dto.request.pin.LockerPinRequest;
import com.zimdugo.locker.entrypoint.dto.response.detail.LockerDetailResponse;
import com.zimdugo.locker.entrypoint.dto.response.keyword.LockerKeywordResponse;
import com.zimdugo.locker.entrypoint.dto.response.pin.LockerPinResponse;
import com.zimdugo.locker.entrypoint.dto.response.place.PlaceLockerResponse;
import com.zimdugo.locker.entrypoint.dto.response.seo.LockerSeoListResponse;
import com.zimdugo.locker.entrypoint.dto.response.suggest.LockerSuggestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Locker", description = "보관함 조회 API")
public interface LockerApi {

    @Operation(
        summary = "보관함 상세 조회",
        description = "보관함 ID로 기본 정보, 위치, 운영 시간, 가격, 크기, 이미지, 정확도 투표 정보를 반환한다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 보관함 ID"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 보관함")
    })
    @SecurityRequirements
    @GetMapping("/lockers/{lockerId}")
    ResponseEntity<RestResponse<LockerDetailResponse>> getLockerDetail(
        @NullableCurrentUser Long userId,
        @PathVariable @Positive Long lockerId
    );

    @Operation(
        summary = "지도 핀 조회",
        description = "현재 지도 화면 영역 내 핀 목록을 반환한다. 줌 레벨 15 이상은 LOCKER/PLACE, 14 이하는 격자 클러스터링을 적용한다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @SecurityRequirements
    @GetMapping("/lockers/pin")
    ResponseEntity<RestResponse<LockerPinResponse>> getPins(
        @NullableCurrentUser Long userId,
        @ParameterObject @Valid LockerPinRequest request
    );

    @Operation(
        summary = "보관함 자동완성 조회",
        description = "검색 점수 우선, 동점 시 현재 좌표 기준 거리순으로 장소/보관함 자동완성 목록을 반환한다."
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
        @Size(max = 100) String keyword
    );

    @Operation(
        summary = "보관함 키워드 검색 조회",
        description = "검색 점수 우선, 동점 시 현재 좌표 기준 거리순으로 장소/보관함 검색 결과를 반환한다. "
            + "사이즈, 실내/실외, 보관함 유형 필터를 함께 적용할 수 있으며 PLACE 결과는 필터에 맞는 거리순 하위 보관함 목록을 반환한다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @SecurityRequirements
    @GetMapping("/lockers/keyword")
    ResponseEntity<RestResponse<LockerKeywordResponse>> getKeywordResults(
        @NullableCurrentUser Long userId,
        @Valid @ParameterObject LockerKeywordRequest request
    );

    @Operation(
        summary = "장소 하위 보관함 조회",
        description = "장소 ID로 필터에 맞는 하위 보관함을 현재 좌표 기준 거리순으로 반환한다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 장소")
    })
    @SecurityRequirements
    @GetMapping("/places/{placeId}")
    ResponseEntity<RestResponse<PlaceLockerResponse>> getPlaceLockers(
        @NullableCurrentUser Long userId,
        @PathVariable Long placeId,
        @Valid @ParameterObject PlaceLockerRequest request
    );

    @Operation(
        summary = "SEO용 보관함 전체 목록 조회",
        description = "SEO를 위해 활성화된 전체 보관함의 최소 정보(ID, 다국어 명칭)를 반환한다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @SecurityRequirements
    @GetMapping("/lockers/seo-list")
    ResponseEntity<RestResponse<LockerSeoListResponse>> getLockerSeoList();
}
