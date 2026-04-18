package com.zimdugo.user.entrypoint;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.user.application.UserProfileDto;
import com.zimdugo.user.application.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserQueryService userQueryService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(
        Authentication authentication
    ) {
        UserProfileDto profile = userQueryService.getProfile(extractUserId(authentication));
        return ResponseEntity.ok(UserProfileResponse.from(profile));
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATED_USER_NOT_FOUND);
        }

        return Long.valueOf(authentication.getName());
    }
}
