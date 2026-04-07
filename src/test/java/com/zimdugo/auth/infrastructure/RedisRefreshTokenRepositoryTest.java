package com.zimdugo.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataRedisTest
@Testcontainers
@Import({
    RedisRefreshTokenRepository.class,
    RedisRefreshTokenRepositoryTest.TestConfig.class
})
class RedisRefreshTokenRepositoryTest {

    @Container
    static GenericContainer<?> redis =
        new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private RedisRefreshTokenRepository repository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    @DisplayName("Refresh token is stored as hash, not raw value")
    void save_storesHashNotRawToken() {
        String rawToken = "raw-refresh-token-value";
        repository.save(1L, "sid-1", rawToken, Duration.ofDays(30));

        String stored = redisTemplate.opsForValue().get("auth:rt:1:sid-1");
        assertThat(stored).isNotNull();
        assertThat(stored).isNotEqualTo(rawToken);
    }

    @Test
    @DisplayName("matches returns true for correct token")
    void matches_returnsTrueForCorrectToken() {
        String rawToken = "raw-refresh-token-value";
        repository.save(1L, "sid-1", rawToken, Duration.ofDays(30));

        assertThat(repository.matches(1L, "sid-1", rawToken)).isTrue();
    }

    @Test
    @DisplayName("matches returns false for wrong token")
    void matches_returnsFalseForWrongToken() {
        repository.save(1L, "sid-1", "correct-token", Duration.ofDays(30));

        assertThat(repository.matches(1L, "sid-1", "wrong-token")).isFalse();
    }

    @Test
    @DisplayName("delete removes one session token")
    void delete_removesToken() {
        repository.save(1L, "sid-1", "token", Duration.ofDays(30));

        repository.delete(1L, "sid-1");

        assertThat(repository.matches(1L, "sid-1", "token")).isFalse();
    }

    @Test
    @DisplayName("deleteAllByUserId removes all tokens for that user only")
    void deleteAllByUserId_removesAllUserTokens() {
        repository.save(1L, "sid-1", "token-1", Duration.ofDays(30));
        repository.save(1L, "sid-2", "token-2", Duration.ofDays(30));
        repository.save(2L, "sid-3", "token-3", Duration.ofDays(30));

        repository.deleteAllByUserId(1L);

        assertThat(repository.matches(1L, "sid-1", "token-1")).isFalse();
        assertThat(repository.matches(1L, "sid-2", "token-2")).isFalse();
        assertThat(repository.matches(2L, "sid-3", "token-3")).isTrue();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
}
