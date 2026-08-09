package com.zimdugo.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.zimdugo.user.domain.AuthProvider;
import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.SocialAccountReader;
import com.zimdugo.user.domain.SocialAccountStore;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.domain.UserStore;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserStore userStore;

    @Mock
    private SocialAccountReader socialAccountReader;

    @Mock
    private SocialAccountStore socialAccountStore;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    @DisplayName("신규 OAuth 사용자는 nickname 없이 생성되고 이메일과 provider 식별값이 저장된다")
    void createsNewOAuthUserWithoutNickname() {
        OAuth2User oauthUser = googleUser("new-user@gmail.com", "provider-user-1", "https://cdn.example.com/p.png");
        OAuth2UserRequest userRequest = googleUserRequest();

        given(socialAccountReader.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "provider-user-1"))
            .willReturn(Optional.empty());
        given(userStore.store(any(User.class)))
            .willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                return new User(1L, user.getEmail(), user.getProfileImageUrl(), user.getStatus(), null, null, null);
            });
        given(socialAccountStore.store(any(SocialAccount.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        try (MockedConstruction<DefaultOAuth2UserService> ignored = mockConstruction(
            DefaultOAuth2UserService.class,
            (mock, context) -> given(mock.loadUser(userRequest)).willReturn(oauthUser)
        )) {
            OAuth2User result = customOAuth2UserService.loadUser(userRequest);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userStore).store(userCaptor.capture());
            assertThat(userCaptor.getValue().getEmail()).isEqualTo("new-user@gmail.com");
            assertThat(userCaptor.getValue().getProfileImageUrl()).isEqualTo("https://cdn.example.com/p.png");
            assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.ACTIVE);

            ArgumentCaptor<SocialAccount> socialCaptor = ArgumentCaptor.forClass(SocialAccount.class);
            verify(socialAccountStore).store(socialCaptor.capture());
            assertThat(socialCaptor.getValue().getProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(socialCaptor.getValue().getProviderUserId()).isEqualTo("provider-user-1");
            assertThat(socialCaptor.getValue().getProviderEmail()).isEqualTo("new-user@gmail.com");

            assertThat(result.getAttributes())
                .containsEntry("userId", 1L)
                .containsEntry("email", "new-user@gmail.com")
                .containsEntry("role", "USER");
        }
    }

    @Test
    @DisplayName("기존 OAuth 사용자는 재생성하지 않고 기존 사용자를 반환한다")
    void returnsExistingOAuthUserWithoutCreatingNewOne() {
        OAuth2User oauthUser = googleUser("existing@gmail.com", "provider-user-2", "https://cdn.example.com/new.png");
        OAuth2UserRequest userRequest = googleUserRequest();
        User existingUser = new User(
            2L,
            "existing@gmail.com",
            "https://cdn.example.com/original.png",
            UserStatus.ACTIVE,
            null,
            null,
            null
        );
        SocialAccount socialAccount = new SocialAccount(
            10L,
            existingUser,
            AuthProvider.GOOGLE,
            "provider-user-2",
            "old@gmail.com",
            "https://cdn.example.com/old.png",
            null
        );

        given(socialAccountReader.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "provider-user-2"))
            .willReturn(Optional.of(socialAccount));
        given(socialAccountStore.store(any(SocialAccount.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        try (MockedConstruction<DefaultOAuth2UserService> ignored = mockConstruction(
            DefaultOAuth2UserService.class,
            (mock, context) -> given(mock.loadUser(userRequest)).willReturn(oauthUser)
        )) {
            OAuth2User result = customOAuth2UserService.loadUser(userRequest);

            verify(userStore, never()).store(any(User.class));

            ArgumentCaptor<SocialAccount> socialCaptor = ArgumentCaptor.forClass(SocialAccount.class);
            verify(socialAccountStore).store(socialCaptor.capture());
            assertThat(socialCaptor.getValue().getProviderEmail()).isEqualTo("existing@gmail.com");
            assertThat(socialCaptor.getValue().getProviderProfileImageUrl())
                .isEqualTo("https://cdn.example.com/new.png");

            assertThat(result.getAttributes())
                .containsEntry("userId", 2L)
                .containsEntry("email", "existing@gmail.com")
                .containsEntry("role", "USER");
        }
    }

    private OAuth2UserRequest googleUserRequest() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
            .clientId("client-id")
            .clientSecret("client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://localhost/login/oauth2/code/google")
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .tokenUri("https://oauth2.googleapis.com/token")
            .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
            .userNameAttributeName("sub")
            .clientName("Google")
            .scope("openid", "profile", "email")
            .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "access-token",
            null,
            null,
            Set.of("openid", "profile", "email")
        );

        return new OAuth2UserRequest(clientRegistration, accessToken);
    }

    private OAuth2User googleUser(String email, String providerUserId, String picture) {
        return new DefaultOAuth2User(
            List.of(),
            Map.of(
                "sub", providerUserId,
                "email", email,
                "picture", picture
            ),
            "email"
        );
    }
}
