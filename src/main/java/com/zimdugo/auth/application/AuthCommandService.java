package com.zimdugo.auth.application;

import com.zimdugo.auth.domain.AuthTokens;
import com.zimdugo.auth.domain.RefreshTokenRepository;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.user.application.UserQueryService;
import com.zimdugo.user.domain.User;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthCommandService {

    private static final long REFRESH_TOKEN_COOKIE_MAX_AGE = 60L * 60L * 24L * 30L;

    private final JwtTokenProvider jwtTokenProvider;
    private final UserQueryService userQueryService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthRefreshResult refresh(String refreshToken) {
        validateRefreshToken(refreshToken);

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String sid = jwtTokenProvider.getSid(refreshToken);

        if (!refreshTokenRepository.matches(userId, sid, refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        User user = userQueryService.findById(userId);
        AuthTokens newTokens = reissueTokens(user, sid);

        return new AuthRefreshResult(
            user.getId(),
            user.getEmail(),
            newTokens.accessToken(),
            newTokens.refreshToken()
        );
    }

    @Transactional
    public void logout(String refreshToken, String accessToken) {
        LogoutTarget logoutTarget = extractLogoutTarget(refreshToken, accessToken);
        if (logoutTarget != null) {
            refreshTokenRepository.delete(logoutTarget.userId(), logoutTarget.sid());
        }
    }

    private void validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private AuthTokens reissueTokens(User user, String sid) {
        AuthTokens newTokens = jwtTokenProvider.generateTokens(
            user.getId(),
            user.getEmail(),
            "USER",
            sid
        );

        refreshTokenRepository.save(
            user.getId(),
            newTokens.sid(),
            newTokens.refreshToken(),
            Duration.ofSeconds(REFRESH_TOKEN_COOKIE_MAX_AGE)
        );

        return newTokens;
    }

    private LogoutTarget extractLogoutTarget(String refreshToken, String accessToken) {
        if (refreshToken != null && !refreshToken.isBlank() && jwtTokenProvider.validateToken(refreshToken)) {
            return new LogoutTarget(
                jwtTokenProvider.getUserId(refreshToken),
                jwtTokenProvider.getSid(refreshToken)
            );
        }

        if (accessToken != null && !accessToken.isBlank() && jwtTokenProvider.validateToken(accessToken)) {
            return new LogoutTarget(
                jwtTokenProvider.getUserId(accessToken),
                jwtTokenProvider.getSid(accessToken)
            );
        }

        return null;
    }

    private record LogoutTarget(Long userId, String sid) {
    }
}
