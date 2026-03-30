package com.zimdugo.auth.infrastructure;

import com.zimdugo.auth.domain.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private final StringRedisTemplate stringRedisTemplate;

    // ── 키 네이밍 ──────────────────────────────────────────
    // auth:rt:{userId}:{sid}      → RT 화이트리스트
    // auth:rjti:{jti}             → jti 재사용 탐지
    // auth:uv:{userId}            → User Version

    @Override
    public void save(Long userId, String sid, String jti, String rawToken, Duration ttl) {
        String key  = rtKey(userId, sid);
        String hash = hash(rawToken);
        stringRedisTemplate.opsForValue().set(key, hash, ttl);
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
    public boolean isJtiUsed(String jti) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(jtiKey(jti))
        );
    }

    @Override
    public void markJtiUsed(String jti, Long userId, String sid, Duration ttl) {
        stringRedisTemplate.opsForValue().set(
                jtiKey(jti),
                userId + ":" + sid,
                ttl
        );
    }

    @Override
    public long getUserVersion(Long userId) {
        String val = stringRedisTemplate.opsForValue().get(uvKey(userId));
        if (val == null) {
            // 최초 로그인 시 1로 초기화
            stringRedisTemplate.opsForValue().set(uvKey(userId), "1");
            return 1L;
        }
        return Long.parseLong(val);
    }

    @Override
    public void incrementUserVersion(Long userId) {
        stringRedisTemplate.opsForValue().increment(uvKey(userId));
        log.info("UV incremented. key={}", uvKey(userId));
    }

    // ── 키 생성 ────────────────────────────────────────────

    private String rtKey(Long userId, String sid) {
        return "auth:rt:" + userId + ":" + sid;
    }

    private String jtiKey(String jti) {
        return "auth:rjti:" + jti;
    }

    private String uvKey(Long userId) {
        return "auth:uv:" + userId;
    }

    // ── SHA-256 해시 (Base64url) ───────────────────────────

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