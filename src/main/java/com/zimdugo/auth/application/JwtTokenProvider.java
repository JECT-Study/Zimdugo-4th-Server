package com.zimdugo.auth.application;

import com.zimdugo.auth.domain.AuthTokens;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 로그인/재발급 시 AT + RT 한 번에 발급.
     * sid는 호출부에서 생성해서 넘기거나, 여기서 새로 만들어도 됨.
     * uv는 Redis에서 조회한 값을 넘겨받음.
     */
    public AuthTokens generateTokens(Long userId, String email, String role, String sid, long uv) {
        String accessJti  = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        String accessToken  = generateAccessToken(userId, email, role, sid, uv, accessJti);
        String refreshToken = generateRefreshToken(userId, sid, uv, refreshJti);

        return new AuthTokens(accessToken, refreshToken, sid, refreshJti);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private String generateAccessToken(Long userId, String email, String role, String sid, long uv, String jti) {
        Instant now    = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.accessTokenExpirationSeconds());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", normalizeRole(role))
                .claim("sid", sid)
                .claim("uv", uv)
                .claim("typ", "AT")
                .id(jti)                        // jti
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    private String generateRefreshToken(Long userId, String sid, long uv, String jti) {
        Instant now    = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.refreshTokenExpirationSeconds());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("sid", sid)
                .claim("uv", uv)
                .claim("typ", "RT")
                .id(jti)                        // jti
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }

    public String getSid(String token) {
        return getClaims(token).get("sid", String.class);
    }

    public String getJti(String token) {
        return getClaims(token).getId();
    }

    public String getEmail(String token) {
        Object email = getClaims(token).get("email");
        return email != null ? email.toString() : null;
    }

    public long getUv(String token) {
        Object uv = getClaims(token).get("uv");
        return uv != null ? ((Number) uv).longValue() : 0L;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Long   userId = Long.valueOf(claims.getSubject());
        String role = normalizeRole(claims.get("role", String.class));

        return new UsernamePasswordAuthenticationToken(
                String.valueOf(userId),
                token,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }
        return role.toUpperCase(Locale.ROOT);
    }
}
