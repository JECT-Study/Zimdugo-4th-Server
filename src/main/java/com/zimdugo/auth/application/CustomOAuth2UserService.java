package com.zimdugo.auth.application;

import com.zimdugo.auth.domain.oauth2.OAuth2UserInfo;
import com.zimdugo.auth.domain.oauth2.OAuth2UserInfoFactory;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.SocialAccountReader;
import com.zimdugo.user.domain.SocialAccountStore;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserRole;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.domain.UserStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String UNUSED_NICKNAME = "unused";

    private final UserStore userStore;
    private final SocialAccountReader socialAccountReader;
    private final SocialAccountStore socialAccountStore;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User);

        validateRequiredFields(userInfo);

        User user = findOrCreateUser(userInfo);
        log.info(
            "OAuth 사용자 동기화가 완료되었습니다. provider={}, userId={}, role={}",
            registrationId,
            user.getId(),
            user.getRoleOrDefault()
        );

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("userId", user.getId());
        attributes.put("email", user.getEmail());
        attributes.put("role", user.getRoleOrDefault().name());

        return new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority(toAuthority(user.getRoleOrDefault()))),
            attributes,
            "email"
        );
    }

    private void validateRequiredFields(OAuth2UserInfo userInfo) {
        if (userInfo.getProviderUserId() == null || userInfo.getProviderUserId().isBlank()) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_user_info"),
                ErrorCode.OAUTH2_INVALID_USER_INFO.getMessage()
            );
        }
        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_user_info"),
                ErrorCode.OAUTH2_INVALID_USER_INFO.getMessage()
            );
        }
    }

    private User findOrCreateUser(OAuth2UserInfo userInfo) {
        return socialAccountReader
            .findByProviderAndProviderUserId(userInfo.getProvider(), userInfo.getProviderUserId())
            .map(socialAccount -> syncAndGetUser(socialAccount, userInfo))
            .orElseGet(() -> createNewUser(userInfo));
    }

    private User syncAndGetUser(SocialAccount socialAccount, OAuth2UserInfo userInfo) {
        socialAccount.updateProviderProfile(
            normalize(userInfo.getEmail()),
            normalize(userInfo.getProfileImageUrl())
        );
        SocialAccount saved = socialAccountStore.store(socialAccount);
        log.debug(
            "OAuth 소셜 계정 동기화가 완료되었습니다. provider={}, userId={}",
            userInfo.getProvider(),
            saved.getUser().getId()
        );
        return saved.getUser();
    }

    private User createNewUser(OAuth2UserInfo userInfo) {
        String email = normalize(userInfo.getEmail());
        String profileImageUrl = normalize(userInfo.getProfileImageUrl());

        User user = new User(
            email,
            UNUSED_NICKNAME,
            profileImageUrl,
            UserStatus.ACTIVE
        );

        User savedUser = userStore.store(user);

        SocialAccount socialAccount = new SocialAccount(
            savedUser,
            userInfo.getProvider(),
            userInfo.getProviderUserId(),
            email,
            profileImageUrl
        );

        socialAccountStore.store(socialAccount);
        log.info(
            "OAuth 신규 사용자가 생성되었습니다. provider={}, userId={}",
            userInfo.getProvider(),
            savedUser.getId()
        );

        return savedUser;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String toAuthority(UserRole role) {
        return "ROLE_" + role.name();
    }
}
