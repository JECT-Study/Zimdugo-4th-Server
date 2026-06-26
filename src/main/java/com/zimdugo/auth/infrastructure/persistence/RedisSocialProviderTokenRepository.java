package com.zimdugo.auth.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.auth.domain.SocialProviderToken;
import com.zimdugo.auth.domain.SocialProviderTokenRepository;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.user.domain.AuthProvider;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisSocialProviderTokenRepository implements SocialProviderTokenRepository {

    private static final String KEY_PREFIX = "auth:social-token:";
    private static final Duration TOKEN_TTL = Duration.ofDays(30);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(Long userId, AuthProvider provider, SocialProviderToken token) {
        stringRedisTemplate.opsForValue().set(key(userId, provider), serialize(token), TOKEN_TTL);
    }

    @Override
    public Optional<SocialProviderToken> find(Long userId, AuthProvider provider) {
        String value = stringRedisTemplate.opsForValue().get(key(userId, provider));
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(deserialize(value));
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        Set<String> keys = stringRedisTemplate.keys(KEY_PREFIX + userId + ":*");
        if (keys == null || keys.isEmpty()) {
            return;
        }
        stringRedisTemplate.delete(keys);
    }

    private String key(Long userId, AuthProvider provider) {
        return KEY_PREFIX + userId + ":" + provider.name();
    }

    private String serialize(SocialProviderToken token) {
        try {
            return objectMapper.writeValueAsString(token);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    private SocialProviderToken deserialize(String value) {
        try {
            return objectMapper.readValue(value, SocialProviderToken.class);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }
}
