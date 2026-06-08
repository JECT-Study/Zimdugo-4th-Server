package com.zimdugo.auth.domain;

import com.zimdugo.user.domain.AuthProvider;

import java.util.Map;

public class FacebookOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.FACEBOOK;
    }

    @Override
    public String getProviderUserId() {
        Object id = attributes.get("id");
        return id != null ? id.toString() : null;
    }

    @Override
    public String getEmail() {
        Object email = attributes.get("email");
        return email != null ? email.toString() : null;
    }

    @Override
    public String getNickname() {
        Object name = attributes.get("name");
        return name != null ? name.toString() : null;
    }

    @Override
    public String getProfileImageUrl() {
        Object picture = attributes.get("picture");

        if (picture instanceof Map<?, ?> picMap) {
            Object data = picMap.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object url = dataMap.get("url");
                return url != null ? url.toString() : null;
            }
        }
        return null;
    }
}
