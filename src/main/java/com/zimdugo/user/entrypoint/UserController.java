package com.zimdugo.user.entrypoint;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.user.application.UserProfileDto;
import com.zimdugo.user.application.UserQueryService;
import com.zimdugo.user.application.UserProfileUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
        Authentication authentication
    ) {
        UserProfileDto profile = userQueryService.getProfile(extractUserId(authentication));
        UserProfileResponse response = UserProfileResponse.from(profile);
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, response));
    }

    @PatchMapping("/me")
    public ResponseEntity<RestResponse<UserProfileResponse>> updateProfile(
        Authentication authentication,
        @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        UserProfileDto profile = userProfileUpdateService.updateProfile(
            extractUserId(authentication),
            request.nickname(),
            request.profileImageUrl()
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, UserProfileResponse.from(profile)));
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATED_USER_NOT_FOUND);
        }

        return Long.valueOf(authentication.getName());
    }
}
