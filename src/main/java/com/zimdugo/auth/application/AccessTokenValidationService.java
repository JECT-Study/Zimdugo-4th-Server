package com.zimdugo.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessTokenValidationService {

    private final JwtTokenProvider jwtTokenProvider;

    public boolean isValidForAuthentication(String accessToken) {
        return jwtTokenProvider.validateToken(accessToken);
    }
}
