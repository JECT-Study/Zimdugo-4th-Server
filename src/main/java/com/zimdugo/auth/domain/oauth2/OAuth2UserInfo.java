package com.zimdugo.auth.domain.oauth2;

import com.zimdugo.user.domain.AuthProvider;

public interface OAuth2UserInfo {

    AuthProvider getProvider();

    String getProviderUserId();

    String getEmail();

    String getNickname();

    String getProfileImageUrl();
}
