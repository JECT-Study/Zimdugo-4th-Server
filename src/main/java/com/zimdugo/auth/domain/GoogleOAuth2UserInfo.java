package com.zimdugo.auth.domain;

import com.zimdugo.user.domain.AuthProvider;

import java.util.Map;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public String getProviderUserId() {
        Object sub = attributes.get("sub");
        return sub != null ? sub.toString() : null;
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
        return picture != null ? picture.toString() : null;
    }
}
