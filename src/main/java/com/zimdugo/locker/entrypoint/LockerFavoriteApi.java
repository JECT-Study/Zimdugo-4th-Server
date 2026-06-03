package com.zimdugo.locker.entrypoint;

import com.zimdugo.core.response.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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
}
