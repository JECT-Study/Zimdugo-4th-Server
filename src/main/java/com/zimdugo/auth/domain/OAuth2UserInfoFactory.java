package com.zimdugo.auth.domain;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
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
            default -> throw new BusinessException(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN);
        };
    }
}
