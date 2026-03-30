package com.zimdugo.auth.domain;

import com.zimdugo.user.domain.AuthProvider;

import java.util.Map;

public class NaverOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.NAVER;
    }

    @Override
    public String getProviderUserId() {
        Map<String, Object> response = getResponse();
        if (response == null) {
            return null;
        }
        Object id = response.get("id");
        return id != null ? id.toString() : null;
    }

    @Override
    public String getEmail() {
        Map<String, Object> response = getResponse();
        if (response == null) {
            return null;
        }
        Object email = response.get("email");
        return email != null ? email.toString() : null;
    }

    @Override
    public String getNickname() {
        Map<String, Object> response = getResponse();
        if (response == null) {
            return null;
        }
        Object nickname = response.get("nickname");
        return nickname != null ? nickname.toString() : null;
    }

    @Override
    public String getProfileImageUrl() {
        Map<String, Object> response = getResponse();
        if (response == null) {
            return null;
        }
        Object profileImage = response.get("profile_image");
        return profileImage != null ? profileImage.toString() : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getResponse() {
        Object response = attributes.get("response");
        if (response instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }
}