package com.zimdugo.auth.application;

import com.zimdugo.auth.domain.RefreshTokenRepository;
import com.zimdugo.auth.domain.SocialProviderTokenRepository;
import com.zimdugo.user.application.UserQueryService;
import com.zimdugo.user.domain.SocialAccountStore;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.domain.UserStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class AccountWithdrawalServiceTest {

    @Mock
    private AccessTokenValidationService accessTokenValidationService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private UserStore userStore;

    @Mock
    private SocialAccountStore socialAccountStore;

    @Mock
    private SocialAccountUnlinkService socialAccountUnlinkService;

    @Mock
    private SocialProviderTokenRepository socialProviderTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AccountWithdrawalService accountWithdrawalService;

    @Test
    void withdraw_unlinksBeforeDeletingInternalState() {
        User user = new User(
            1L,
            "user@zimdugo.com",
            "zimdugo",
            "profile",
            UserStatus.ACTIVE,
            null,
            null,
            null
        );

        given(accessTokenValidationService.isValidForAuthentication("valid-at")).willReturn(true);
        given(jwtTokenProvider.getUserId("valid-at")).willReturn(1L);
        given(userQueryService.findById(1L)).willReturn(user);

        accountWithdrawalService.withdraw("valid-at");

        InOrder inOrder = inOrder(
            socialAccountUnlinkService,
            userStore,
            socialAccountStore,
            socialProviderTokenRepository,
            refreshTokenRepository
        );
        inOrder.verify(socialAccountUnlinkService).unlinkAll(1L);
        inOrder.verify(userStore).store(user);
        inOrder.verify(socialAccountStore).deleteAllByUserId(1L);
        inOrder.verify(socialProviderTokenRepository).deleteAllByUserId(1L);
        inOrder.verify(refreshTokenRepository).deleteAllByUserId(1L);
    }
}
