package com.zimdugo.auth.entrypoint.oauth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int AUTH_REQUEST_COOKIE_MAX_AGE_SECONDS = 300;
    private static final String SAME_SITE_POLICY = "Lax";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final byte[] signingKey;

    public HttpCookieOAuth2AuthorizationRequestRepository(
        ObjectMapper objectMapper,
        @Value("${auth.oauth2.authorization-request-signing-key:${jwt.secret}}") String signingKey
    ) {
        this.objectMapper = objectMapper;
        this.signingKey = signingKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (AUTH_REQUEST_COOKIE_NAME.equals(cookie.getName())) {
                return deserialize(cookie.getValue());
            }
        }

        return null;
    }

    @Override
    public void saveAuthorizationRequest(
        OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        if (authorizationRequest == null) {
            removeAuthorizationRequest(request, response);
            return;
        }

        addCookie(response, serialize(authorizationRequest), AUTH_REQUEST_COOKIE_MAX_AGE_SECONDS, request.isSecure());
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        addCookie(response, "", 0, request.isSecure());
        return authorizationRequest;
    }

    private void addCookie(HttpServletResponse response, String value, int maxAgeSeconds, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(AUTH_REQUEST_COOKIE_NAME, value)
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .maxAge(maxAgeSeconds)
            .sameSite(SAME_SITE_POLICY)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        AuthorizationRequestCookiePayload payload = AuthorizationRequestCookiePayload.from(authorizationRequest);

        try {
            String json = objectMapper.writeValueAsString(payload);
            String encodedPayload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
            String signature = sign(encodedPayload);

            return encodedPayload + "." + signature;
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    private OAuth2AuthorizationRequest deserialize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String[] parts = value.split("\\.", 2);
        if (parts.length != 2) {
            log.warn("OAuth2 인가 요청 쿠키 형식이 올바르지 않습니다.");
            return null;
        }

        String encodedPayload = parts[0];
        String signature = parts[1];

        if (!isValidSignature(encodedPayload, signature)) {
            log.warn("OAuth2 인가 요청 쿠키 서명 검증에 실패했습니다.");
            return null;
        }

        try {
            byte[] jsonBytes = Base64.getUrlDecoder().decode(encodedPayload);
            AuthorizationRequestCookiePayload payload = objectMapper.readValue(
                jsonBytes,
                AuthorizationRequestCookiePayload.class
            );

            return payload.toAuthorizationRequest();
        } catch (IllegalArgumentException e) {
            log.warn("OAuth2 인가 요청 쿠키 payload Base64 디코딩에 실패했습니다.", e);
            return null;
        } catch (IOException e) {
            log.warn("OAuth2 인가 요청 쿠키 payload JSON 역직렬화에 실패했습니다.", e);
            return null;
        }
    }

    private boolean isValidSignature(String payload, String signature) {
        String expected = sign(payload);
        return MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            signature.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(signingKey, HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    private record AuthorizationRequestCookiePayload(
        String authorizationUri,
        String clientId,
        String redirectUri,
        String state,
        String authorizationRequestUri,
        Map<String, Object> additionalParameters,
        Map<String, Object> attributes
    ) {
        private static AuthorizationRequestCookiePayload from(OAuth2AuthorizationRequest authorizationRequest) {
            return new AuthorizationRequestCookiePayload(
                authorizationRequest.getAuthorizationUri(),
                authorizationRequest.getClientId(),
                authorizationRequest.getRedirectUri(),
                authorizationRequest.getState(),
                authorizationRequest.getAuthorizationRequestUri(),
                authorizationRequest.getAdditionalParameters(),
                authorizationRequest.getAttributes()
            );
        }

        private OAuth2AuthorizationRequest toAuthorizationRequest() {
            return OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(authorizationUri)
                .clientId(clientId)
                .redirectUri(redirectUri)
                .state(state)
                .authorizationRequestUri(authorizationRequestUri)
                .additionalParameters(coerceMap(additionalParameters))
                .attributes(coerceMap(attributes))
                .build();
        }

        private static Map<String, Object> coerceMap(Map<String, Object> source) {
            return source == null ? Map.of() : source;
        }
    }
}
