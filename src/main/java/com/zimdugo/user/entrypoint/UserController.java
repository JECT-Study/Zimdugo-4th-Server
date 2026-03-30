package com.zimdugo.user.entrypoint;

import com.zimdugo.auth.domain.JwtPrincipal;
import com.zimdugo.user.application.UserQueryService;
import com.zimdugo.user.domain.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return ResponseEntity.ok(userQueryService.getProfile(principal.userId()));
    }
}