package com.zimdugo.auth.domain;

import com.zimdugo.user.domain.AuthProvider;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.KAKAO;
    }

    @Override
    public String getProviderUserId() {
        Object id = attributes.get("id");
        return id != null ? id.toString() : null;
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = getKakaoAccount();
        if (kakaoAccount == null) {
            return null;
        }
        Object email = kakaoAccount.get("email");
        return email != null ? email.toString() : null;
    }

    @Override
    public String getNickname() {
        Map<String, Object> profile = getProfile();
        if (profile == null) {
            return null;
        }
        Object nickname = profile.get("nickname");
        return nickname != null ? nickname.toString() : null;
    }

    @Override
    public String getProfileImageUrl() {
        Map<String, Object> profile = getProfile();
        if (profile == null) {
            return null;
        }
        Object profileImageUrl = profile.get("profile_image_url");
        return profileImageUrl != null ? profileImageUrl.toString() : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getKakaoAccount() {
        Object kakaoAccount = attributes.get("kakao_account");
        if (kakaoAccount instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getProfile() {
        Map<String, Object> kakaoAccount = getKakaoAccount();
        if (kakaoAccount == null) {
            return null;
        }

        Object profile = kakaoAccount.get("profile");
        if (profile instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }
}
