package com.zimdugo.auth.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
            "test-secret-key-must-be-32-bytes!!",
            900L,
            2592000L
        );
        jwtTokenProvider = new JwtTokenProvider(properties);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("AT 생성 시 클레임이 올바르게 설정된다")
    void generateAccessToken_claimsAreCorrect() {
        Long userId = 1L;
        String email = "test@zimdugo.com";
        String sid = "test-sid";
        long uv = 1L;

        var tokens = jwtTokenProvider.generateTokens(userId, email, "USER", sid, uv);
        String at = tokens.accessToken();

        assertThat(jwtTokenProvider.validateToken(at)).isTrue();
        assertThat(jwtTokenProvider.getUserId(at)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getSid(at)).isEqualTo(sid);
        assertThat(jwtTokenProvider.getUv(at)).isEqualTo(uv);
        assertThat(jwtTokenProvider.getClaims(at).get("role", String.class)).isEqualTo("USER");
        assertThat(jwtTokenProvider.getEmail(at)).isEqualTo(email);
    }

    @Test
    @DisplayName("RT 생성 시 클레임이 올바르게 설정된다")
    void generateRefreshToken_claimsAreCorrect() {
        Long userId = 1L;
        String sid = "test-sid";

        var tokens = jwtTokenProvider.generateTokens(userId, null, "USER", sid, 1L);
        String rt = tokens.refreshToken();

        assertThat(jwtTokenProvider.validateToken(rt)).isTrue();
        assertThat(jwtTokenProvider.getUserId(rt)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getSid(rt)).isEqualTo(sid);
        assertThat(jwtTokenProvider.getUv(rt)).isEqualTo(1L);
        assertThat(jwtTokenProvider.getJti(rt)).isNotNull();
    }

    @Test
    @DisplayName("AT와 RT의 jti는 서로 다르다")
    void accessTokenAndRefreshToken_haveDifferentJti() {
        var tokens = jwtTokenProvider.generateTokens(1L, null, "USER", "sid", 1L);

        assertThat(jwtTokenProvider.getJti(tokens.accessToken()))
            .isNotEqualTo(jwtTokenProvider.getJti(tokens.refreshToken()));
    }

    @Test
    @DisplayName("변조된 토큰은 검증에 실패한다")
    void tamperedToken_failsValidation() {
        var tokens = jwtTokenProvider.generateTokens(1L, null, "USER", "sid", 1L);
        String tampered = tokens.accessToken() + "tampered";

        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("형식이 잘못된 문자열은 검증에 실패한다")
    void malformedToken_failsValidation() {
        assertThat(jwtTokenProvider.validateToken("not-a-jwt-token")).isFalse();
    }

    @Test
    @DisplayName("빈 문자열 토큰은 검증에 실패한다")
    void blankToken_failsValidation() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 검증에 실패한다")
    void expiredToken_failsValidation() {
        JwtProperties expiredProps = new JwtProperties(
            "test-secret-key-must-be-32-bytes!!",
            0L,
            0L
        );
        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);
        expiredProvider.init();

        var tokens = expiredProvider.generateTokens(1L, null, "USER", "sid", 1L);

        assertThat(expiredProvider.validateToken(tokens.accessToken())).isFalse();
    }

    @Test
    @DisplayName("재발급마다 새로운 sid와 jti가 생성된다")
    void eachIssuance_generatesDifferentSidAndJti() {
        var tokens1 = jwtTokenProvider.generateTokens(1L, null, "USER", "sid-1", 1L);
        var tokens2 = jwtTokenProvider.generateTokens(1L, null, "USER", "sid-2", 1L);

        assertThat(tokens1.refreshJti()).isNotEqualTo(tokens2.refreshJti());
        assertThat(tokens1.sid()).isNotEqualTo(tokens2.sid());
    }

    @Test
    @DisplayName("AT role 클레임이 ADMIN이면 ROLE_ADMIN 권한으로 인증된다")
    void getAuthentication_adminRoleMapped() {
        var tokens = jwtTokenProvider.generateTokens(1L, null, "ADMIN", "sid", 1L);

        var authentication = jwtTokenProvider.getAuthentication(tokens.accessToken());

        assertThat(authentication.getAuthorities())
            .extracting(authority -> authority.getAuthority())
            .containsExactly("ROLE_ADMIN");
    }
}
