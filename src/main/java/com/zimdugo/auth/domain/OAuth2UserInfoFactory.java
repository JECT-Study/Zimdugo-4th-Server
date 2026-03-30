package com.zimdugo.auth.domain;

import org.springframework.security.oauth2.core.user.OAuth2User;

public class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
    }

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, OAuth2User oAuth2User) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
            case "naver" -> new NaverOAuth2UserInfo(oAuth2User.getAttributes());
            case "kakao" -> new KakaoOAuth2UserInfo(oAuth2User.getAttributes());
            case "facebook" -> new FacebookOAuth2UserInfo(oAuth2User.getAttributes());
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);

        };
    }
}