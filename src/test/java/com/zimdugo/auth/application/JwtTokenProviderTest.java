package com.zimdugo.auth.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("Access token contains expected claims")
    void generateAccessToken_claimsAreCorrect() {
        Long userId = 1L;
        String email = "test@zimdugo.com";
        String sid = "test-sid";

        var tokens = jwtTokenProvider.generateTokens(userId, email, "USER", sid);
        String at = tokens.accessToken();

        assertThat(jwtTokenProvider.validateToken(at)).isTrue();
        assertThat(jwtTokenProvider.getUserId(at)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getSid(at)).isEqualTo(sid);
        assertThat(jwtTokenProvider.getClaims(at).get("role", String.class)).isEqualTo("USER");
        assertThat(jwtTokenProvider.getClaims(at).get("typ", String.class)).isEqualTo("AT");
        assertThat(jwtTokenProvider.getClaims(at).get("email", String.class)).isEqualTo(email);
    }

    @Test
    @DisplayName("Refresh token contains expected claims")
    void generateRefreshToken_claimsAreCorrect() {
        Long userId = 1L;
        String sid = "test-sid";

        var tokens = jwtTokenProvider.generateTokens(userId, null, "USER", sid);
        String rt = tokens.refreshToken();

        assertThat(jwtTokenProvider.validateToken(rt)).isTrue();
        assertThat(jwtTokenProvider.getUserId(rt)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getSid(rt)).isEqualTo(sid);
        assertThat(jwtTokenProvider.getClaims(rt).get("typ", String.class)).isEqualTo("RT");
        assertThat(jwtTokenProvider.getClaims(rt).getId()).isNotBlank();
    }

    @Test
    @DisplayName("AT and RT have different jti values")
    void accessTokenAndRefreshToken_haveDifferentJti() {
        var tokens = jwtTokenProvider.generateTokens(1L, null, "USER", "sid");

        assertThat(jwtTokenProvider.getClaims(tokens.accessToken()).getId())
            .isNotEqualTo(jwtTokenProvider.getClaims(tokens.refreshToken()).getId());
    }

    @Test
    @DisplayName("Tampered token fails validation")
    void tamperedToken_failsValidation() {
        var tokens = jwtTokenProvider.generateTokens(1L, null, "USER", "sid");
        String tampered = tokens.accessToken() + "tampered";

        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("Malformed token fails validation")
    void malformedToken_failsValidation() {
        assertThat(jwtTokenProvider.validateToken("not-a-jwt-token")).isFalse();
    }

    @Test
    @DisplayName("Expired token fails validation")
    void expiredToken_failsValidation() {
        JwtProperties expiredProps = new JwtProperties(
            "test-secret-key-must-be-32-bytes!!",
            0L,
            0L
        );
        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);
        expiredProvider.init();

        var tokens = expiredProvider.generateTokens(1L, null, "USER", "sid");
        assertThat(expiredProvider.validateToken(tokens.accessToken())).isFalse();
    }

    @Test
    @DisplayName("Admin role maps to ROLE_ADMIN authority")
    void getAuthentication_adminRoleMapped() {
        var tokens = jwtTokenProvider.generateTokens(1L, null, "ADMIN", "sid");

        var authentication = jwtTokenProvider.getAuthentication(tokens.accessToken());

        assertThat(authentication.getAuthorities())
            .extracting(authority -> authority.getAuthority())
            .containsExactly("ROLE_ADMIN");
    }
}
