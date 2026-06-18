package com.zimdugo.user.entrypoint;

import com.zimdugo.user.entrypoint.dto.request.UserProfileUpdateRequest;
import com.zimdugo.user.entrypoint.dto.response.UserProfileResponse;

import com.zimdugo.common.security.CurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.user.application.UserProfileDto;
import com.zimdugo.user.application.UserQueryService;
import com.zimdugo.user.application.UserProfileUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserQueryService userQueryService;
    private final UserProfileUpdateService userProfileUpdateService;

    @GetMapping("/me")
    public ResponseEntity<RestResponse<UserProfileResponse>> me(
        @CurrentUser Long userId
    ) {
        UserProfileDto profile = userQueryService.getProfile(userId);
        UserProfileResponse response = UserProfileResponse.from(profile);
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, response));
    }

    @PatchMapping("/me")
    public ResponseEntity<RestResponse<UserProfileResponse>> updateProfile(
        @CurrentUser Long userId,
        @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        UserProfileDto profile = userProfileUpdateService.updateProfile(
            userId,
            request.nickname(),
            request.profileImageUrl()
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, UserProfileResponse.from(profile)));
    }
}
