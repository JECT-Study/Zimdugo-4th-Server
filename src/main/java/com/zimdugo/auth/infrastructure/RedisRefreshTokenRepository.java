package com.zimdugo.auth.infrastructure;

import com.zimdugo.auth.domain.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(Long userId, String sid, String rawToken, Duration ttl) {
        String key = rtKey(userId, sid);
        stringRedisTemplate.opsForValue().set(key, hash(rawToken), ttl);
        log.info("RT saved. key={}", key);
    }

    @Override
    public boolean matches(Long userId, String sid, String rawToken) {
        String saved = stringRedisTemplate.opsForValue().get(rtKey(userId, sid));
        return saved != null && saved.equals(hash(rawToken));
    }

    @Override
    public void delete(Long userId, String sid) {
        Boolean deleted = stringRedisTemplate.delete(rtKey(userId, sid));
        log.info("RT deleted. key={}, result={}", rtKey(userId, sid), deleted);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        Set<String> keys = stringRedisTemplate.keys("auth:rt:" + userId + ":*");
        if (keys == null || keys.isEmpty()) {
            return;
        }
        Long deletedCount = stringRedisTemplate.delete(keys);
        log.info("All RT deleted for user. userId={}, count={}", userId, deletedCount);
    }

    private String rtKey(Long userId, String sid) {
        return "auth:rt:" + userId + ":" + sid;
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
