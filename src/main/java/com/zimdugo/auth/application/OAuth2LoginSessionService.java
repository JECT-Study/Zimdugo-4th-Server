package com.zimdugo.auth.application;

import com.zimdugo.auth.domain.AuthTokens;
import com.zimdugo.auth.domain.RefreshTokenRepository;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2LoginSessionService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public OAuth2LoginSessionResult createSession(Long userId, String email, String role) {
        String sid = UUID.randomUUID().toString();
        AuthTokens tokens = jwtTokenProvider.generateTokens(userId, email, role, sid);
        Duration rtTtl = Duration.ofSeconds(jwtProperties.refreshTokenExpirationSeconds());
        refreshTokenRepository.save(userId, sid, tokens.refreshToken(), rtTtl);

        return new OAuth2LoginSessionResult(tokens.refreshToken(), rtTtl, sid);
    }
}
