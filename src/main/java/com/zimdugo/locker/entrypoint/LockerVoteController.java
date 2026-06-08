package com.zimdugo.locker.entrypoint;

import com.zimdugo.common.security.CurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.LockerVoteCommandService;
import com.zimdugo.locker.entrypoint.dto.request.LockerVoteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LockerVoteController implements LockerVoteApi {

    private final LockerVoteCommandService lockerVoteCommandService;

    @Override
    public ResponseEntity<RestResponse<Void>> toggleVote(
        @CurrentUser Long userId,
        Long lockerId,
        LockerVoteRequest request
    ) {
        lockerVoteCommandService.toggleVote(userId, lockerId, request.voteType());
        return ResponseEntity.ok(RestResponse.ok(SuccessCode.OK));
    }
}
