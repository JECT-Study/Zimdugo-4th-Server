package com.zimdugo.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.zimdugo.auth.domain.SocialAccountUnlinkClient;
import com.zimdugo.auth.domain.SocialProviderToken;
import com.zimdugo.auth.domain.SocialProviderTokenRepository;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.user.domain.AuthProvider;
import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.SocialAccountReader;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SocialAccountUnlinkServiceTest {

    @Mock
    private SocialAccountReader socialAccountReader;

    @Mock
    private SocialProviderTokenRepository socialProviderTokenRepository;

    @Mock
    private SocialAccountUnlinkClient googleUnlinkClient;

    private SocialAccountUnlinkService socialAccountUnlinkService;

    @BeforeEach
    void setUp() {
        given(googleUnlinkClient.provider()).willReturn(AuthProvider.GOOGLE);
        socialAccountUnlinkService = new SocialAccountUnlinkService(
            socialAccountReader,
            socialProviderTokenRepository,
            List.of(googleUnlinkClient)
        );
    }

    @Test
    @DisplayName("skips when unlink client is missing")
    void skipsWhenUnlinkClientIsMissing() {
        SocialAccount socialAccount = socialAccount(AuthProvider.FACEBOOK);
        given(socialAccountReader.findByUserId(1L)).willReturn(Optional.of(socialAccount));

        SocialAccountUnlinkSummary summary = socialAccountUnlinkService.unlinkAll(1L);

        assertThat(summary.unlinkedCount()).isZero();
        assertThat(summary.skippedUnsupportedProviderCount()).isEqualTo(1);
        assertThat(summary.skippedMissingTokenCount()).isZero();
        assertThat(summary.failedExternalCount()).isZero();
    }

    @Test
    @DisplayName("skips when provider token is missing")
    void skipsWhenProviderTokenIsMissing() {
        SocialAccount socialAccount = socialAccount(AuthProvider.GOOGLE);
        given(socialAccountReader.findByUserId(1L)).willReturn(Optional.of(socialAccount));
        given(socialProviderTokenRepository.find(1L, AuthProvider.GOOGLE)).willReturn(Optional.empty());

        SocialAccountUnlinkSummary summary = socialAccountUnlinkService.unlinkAll(1L);

        assertThat(summary.unlinkedCount()).isZero();
        assertThat(summary.skippedUnsupportedProviderCount()).isZero();
        assertThat(summary.skippedMissingTokenCount()).isEqualTo(1);
        assertThat(summary.failedExternalCount()).isZero();
        verify(googleUnlinkClient, never()).unlink(any(), any());
    }

    @Test
    @DisplayName("continues when external unlink fails")
    void continuesWhenExternalUnlinkFails() {
        SocialAccount socialAccount = socialAccount(AuthProvider.GOOGLE);
        SocialProviderToken token = token();

        given(socialAccountReader.findByUserId(1L)).willReturn(Optional.of(socialAccount));
        given(socialProviderTokenRepository.find(1L, AuthProvider.GOOGLE)).willReturn(Optional.of(token));
        doThrow(new BusinessException(ErrorCode.EXTERNAL_API_ERROR))
            .when(googleUnlinkClient)
            .unlink(socialAccount, token);

        SocialAccountUnlinkSummary summary = socialAccountUnlinkService.unlinkAll(1L);

        assertThat(summary.unlinkedCount()).isZero();
        assertThat(summary.skippedUnsupportedProviderCount()).isZero();
        assertThat(summary.skippedMissingTokenCount()).isZero();
        assertThat(summary.failedExternalCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("propagates unexpected runtime exception")
    void propagatesUnexpectedRuntimeException() {
        SocialAccount socialAccount = socialAccount(AuthProvider.GOOGLE);
        SocialProviderToken token = token();

        given(socialAccountReader.findByUserId(1L)).willReturn(Optional.of(socialAccount));
        given(socialProviderTokenRepository.find(1L, AuthProvider.GOOGLE)).willReturn(Optional.of(token));
        doThrow(new IllegalStateException("unexpected"))
            .when(googleUnlinkClient)
            .unlink(socialAccount, token);

        assertThatThrownBy(() -> socialAccountUnlinkService.unlinkAll(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("unexpected");
    }

    @Test
    @DisplayName("social account가 없으면 빈 요약을 반환한다")
    void returnsEmptySummaryWhenSocialAccountDoesNotExist() {
        given(socialAccountReader.findByUserId(1L)).willReturn(Optional.empty());

        SocialAccountUnlinkSummary summary = socialAccountUnlinkService.unlinkAll(1L);

        assertThat(summary.unlinkedCount()).isZero();
        assertThat(summary.skippedUnsupportedProviderCount()).isZero();
        assertThat(summary.skippedMissingTokenCount()).isZero();
        assertThat(summary.failedExternalCount()).isZero();
        verify(googleUnlinkClient, never()).unlink(any(), any());
    }

    private SocialAccount socialAccount(AuthProvider provider) {
        User user = new User(1L, "user@example.com", null, UserStatus.ACTIVE, null, null, null);
        return new SocialAccount(1L, user, provider, "provider-user-id", "user@example.com", null, null);
    }

    private SocialProviderToken token() {
        return new SocialProviderToken("access-token", Instant.now().plusSeconds(300), "refresh-token");
    }
}
