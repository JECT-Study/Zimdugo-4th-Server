package com.zimdugo.auth.application;

import com.zimdugo.auth.domain.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessTokenValidationService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public boolean isValidForAuthentication(String accessToken) {
        if (!jwtTokenProvider.validateToken(accessToken)) {
            return false;
        }

        Long userId = jwtTokenProvider.getUserId(accessToken);
        long tokenUv = jwtTokenProvider.getUv(accessToken);
        long currentUv = refreshTokenRepository.getUserVersion(userId);
        return tokenUv == currentUv;
    }
}
