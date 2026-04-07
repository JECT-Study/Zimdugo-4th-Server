package com.zimdugo.auth.application;

import com.zimdugo.auth.domain.RefreshTokenRepository;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.user.application.UserQueryService;
import com.zimdugo.user.domain.SocialAccountStore;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.domain.UserStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountWithdrawalService {

    private final AccessTokenValidationService accessTokenValidationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserQueryService userQueryService;
    private final UserStore userStore;
    private final SocialAccountStore socialAccountStore;
    private final RefreshTokenRepository refreshTokenRepository;

    public void withdraw(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!accessTokenValidationService.isValidForAuthentication(accessToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = jwtTokenProvider.getUserId(accessToken);
        User user = userQueryService.findById(userId);
        if (user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException(ErrorCode.USER_ALREADY_WITHDRAWN);
        }

        user.changeStatus(UserStatus.DELETED);
        userStore.store(user);

        socialAccountStore.deleteAllByUserId(userId);
        refreshTokenRepository.deleteAllByUserId(userId);
    }
}
