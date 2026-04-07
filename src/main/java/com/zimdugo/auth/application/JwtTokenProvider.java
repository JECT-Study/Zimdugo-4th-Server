package com.zimdugo.auth.application;

import com.zimdugo.auth.domain.AuthTokens;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public AuthTokens generateTokens(Long userId, String email, String role, String sid) {
        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        String accessToken = generateAccessToken(userId, email, role, sid, accessJti);
        String refreshToken = generateRefreshToken(userId, sid, refreshJti);

        return new AuthTokens(accessToken, refreshToken, sid);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private String generateAccessToken(Long userId, String email, String role, String sid, String jti) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.accessTokenExpirationSeconds());

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("email", email)
            .claim("role", normalizeRole(role))
            .claim("sid", sid)
            .claim("typ", "AT")
            .id(jti)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(secretKey)
            .compact();
    }

    private String generateRefreshToken(Long userId, String sid, String jti) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.refreshTokenExpirationSeconds());

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("sid", sid)
            .claim("typ", "RT")
            .id(jti)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(secretKey)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public Long getUserId(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }

    public String getSid(String token) {
        return getClaims(token).get("sid", String.class);
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Long userId = Long.valueOf(claims.getSubject());
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
