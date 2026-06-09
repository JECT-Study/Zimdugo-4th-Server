package com.zimdugo.locker.entrypoint;

import com.zimdugo.common.security.CurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.locker.entrypoint.dto.response.mypage.MyPageSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

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
}
