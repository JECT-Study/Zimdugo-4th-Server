package com.zimdugo.auth.application;

import com.zimdugo.auth.domain.OAuth2UserInfo;
import com.zimdugo.auth.domain.OAuth2UserInfoFactory;
import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserRole;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.infrastructure.SocialAccountJpaRepository;
import com.zimdugo.user.infrastructure.UserJpaRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserJpaRepository userJpaRepository;
    private final SocialAccountJpaRepository socialAccountJpaRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User);

        validateRequiredFields(userInfo, registrationId);

        User user = findOrCreateUser(userInfo);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("userId", user.getId());
        attributes.put("email", user.getEmail());
        attributes.put("nickname", user.getNickname());
        attributes.put("role", user.getRoleOrDefault().name());

        String nameAttributeKey = resolveNameAttributeKey(user);

        return new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority(toAuthority(user.getRoleOrDefault()))),
            attributes,
            nameAttributeKey
        );
    }

    private void validateRequiredFields(OAuth2UserInfo userInfo, String registrationId) {
        if (userInfo.getProviderUserId() == null || userInfo.getProviderUserId().isBlank()) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_user_info"),
                registrationId + " 사용자 식별자(providerUserId)를 가져오지 못했습니다."
            );
        }
    }

    private User findOrCreateUser(OAuth2UserInfo userInfo) {
        return socialAccountJpaRepository
            .findByProviderAndProviderUserId(userInfo.getProvider(), userInfo.getProviderUserId())
            .map(SocialAccount::getUser)
            .orElseGet(() -> createNewUser(userInfo));
    }

    private User createNewUser(OAuth2UserInfo userInfo) {
        String email = normalize(userInfo.getEmail());
        String nickname = resolveNickname(userInfo);
        String profileImageUrl = normalize(userInfo.getProfileImageUrl());

        User user = new User(email, nickname, profileImageUrl, UserStatus.ACTIVE);
        User savedUser = userJpaRepository.save(user);

        SocialAccount socialAccount = new SocialAccount(
            savedUser,
            userInfo.getProvider(),
            userInfo.getProviderUserId(),
            email,
            profileImageUrl
        );
        socialAccountJpaRepository.save(socialAccount);

        return savedUser;
    }

    private String resolveNickname(OAuth2UserInfo userInfo) {
        String nickname = normalize(userInfo.getNickname());
        if (nickname != null) {
            return nickname;
        }

        String email = normalize(userInfo.getEmail());
        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }

        return userInfo.getProvider().name().toLowerCase() + "_" + userInfo.getProviderUserId();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String resolveNameAttributeKey(User user) {
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return "email";
        }
        return "userId";
    }

    private String toAuthority(UserRole role) {
        return "ROLE_" + role.name();
    }
}
