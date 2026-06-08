package com.zimdugo.locker.entrypoint;

import com.zimdugo.common.security.CurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.locker.entrypoint.dto.request.LockerVoteRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Locker Vote", description = "보관함 투표 API")
public interface LockerVoteApi {

    @Operation(
        summary = "보관함 정확도 투표 토글",
        description = "보관함에 대해 정확해요(CORRECT) / 부정확해요(INCORRECT) 투표를 토글합니다. 이미 동일한 투표가 존재하면 취소하고, 다른 투표가 존재하면 변경합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "투표 처리 완료"),
        @ApiResponse(responseCode = "401", description = "로그인 필요"),
        @ApiResponse(responseCode = "404", description = "보관함이 존재하지 않음")
    })
    @PostMapping("/me/lockers/{lockerId}/votes")
    ResponseEntity<RestResponse<Void>> toggleVote(
        @CurrentUser Long userId,
        @PathVariable("lockerId") @Positive Long lockerId,
        @Valid @RequestBody LockerVoteRequest request
    );
}
