package com.zimdugo.auth.application;

import com.zimdugo.common.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    public void save(Long userId, String refreshToken) {
        log.info("refresh token saved. userId={}, key={}", userId, key(userId));

        stringRedisTemplate.opsForValue().set(
                key(userId),
                refreshToken,
                Duration.ofSeconds(jwtProperties.refreshTokenExpirationSeconds())
        );
    }

    public String get(Long userId) {
        return stringRedisTemplate.opsForValue().get(key(userId));
    }

    public boolean matches(Long userId, String refreshToken) {
        String saved = get(userId);
        return saved != null && saved.equals(refreshToken);
    }

    public boolean delete(Long userId) {
        Boolean deleted = stringRedisTemplate.delete(key(userId));
        boolean result = Boolean.TRUE.equals(deleted);

        log.info("refresh token deleted. userId={}, key={}, result={}", userId, key(userId), result);

        return result;
    }

    private String key(Long userId) {
        return "auth:refresh:" + userId;
    }
}
