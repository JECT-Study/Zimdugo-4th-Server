package com.zimdugo.auth.infrastructure;

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

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

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
        redisTemplate.getConnectionFactory()
            .getConnection()
            .serverCommands()
            .flushAll();
    }

    @Test
    @DisplayName("RT 저장 시 해시값으로 저장되고 원문은 저장되지 않는다")
    void save_storesHashNotRawToken() {
        String rawToken = "raw-refresh-token-value";

        repository.save(1L, "sid-1", "jti-1", rawToken, Duration.ofDays(30));

        String stored = redisTemplate.opsForValue().get("auth:rt:1:sid-1");
        assertThat(stored).isNotNull();
        assertThat(stored).isNotEqualTo(rawToken);
    }

    @Test
    @DisplayName("저장된 RT와 원문을 비교하면 일치한다")
    void matches_returnsTrueForCorrectToken() {
        String rawToken = "raw-refresh-token-value";
        repository.save(1L, "sid-1", "jti-1", rawToken, Duration.ofDays(30));

        assertThat(repository.matches(1L, "sid-1", rawToken)).isTrue();
    }

    @Test
    @DisplayName("다른 토큰과 비교하면 불일치한다")
    void matches_returnsFalseForWrongToken() {
        repository.save(1L, "sid-1", "jti-1", "correct-token", Duration.ofDays(30));

        assertThat(repository.matches(1L, "sid-1", "wrong-token")).isFalse();
    }

    @Test
    @DisplayName("RT 삭제 후 조회하면 불일치한다")
    void delete_removesToken() {
        repository.save(1L, "sid-1", "jti-1", "token", Duration.ofDays(30));

        repository.delete(1L, "sid-1");

        assertThat(repository.matches(1L, "sid-1", "token")).isFalse();
    }

    @Test
    @DisplayName("사용된 jti는 재사용 탐지에서 true를 반환한다")
    void isJtiUsed_returnsTrueAfterMarkJtiUsed() {
        assertThat(repository.isJtiUsed("jti-1")).isFalse();

        repository.markJtiUsed("jti-1", 1L, "sid-1", Duration.ofDays(30));

        assertThat(repository.isJtiUsed("jti-1")).isTrue();
    }

    @Test
    @DisplayName("uv는 최초 조회 시 1로 초기화된다")
    void getUserVersion_initializesToOne() {
        assertThat(repository.getUserVersion(1L)).isEqualTo(1L);
    }

    @Test
    @DisplayName("uv 증가 시 1씩 올라간다")
    void incrementUserVersion_incrementsByOne() {
        repository.getUserVersion(1L);

        repository.incrementUserVersion(1L);

        assertThat(repository.getUserVersion(1L)).isEqualTo(2L);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
}
