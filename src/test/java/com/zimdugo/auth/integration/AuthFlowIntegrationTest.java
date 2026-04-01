package com.zimdugo.auth.integration;

import com.zimdugo.auth.application.JwtTokenProvider;
import com.zimdugo.auth.domain.AuthTokens;
import com.zimdugo.auth.domain.RefreshTokenRepository;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.infrastructure.UserJpaRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Field;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Testcontainers
class AuthFlowIntegrationTest {

    @Container
    static GenericContainer<?> redis =
        new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        stringRedisTemplate.getConnectionFactory()
            .getConnection()
            .serverCommands()
            .flushAll();

        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("유효한 RT로 refresh 요청 시 새 access token을 발급한다")
    void refresh_withValidRefreshToken_returnsNewAccessToken() throws Exception {
        User savedUser = userJpaRepository.save(
            new User(
                "test@zimdugo.com",
                "테스트",
                null,
                UserStatus.ACTIVE
            )
        );

        Long userId = extractUserId(savedUser);
        assertThat(userId).isNotNull();

        AuthTokens tokens = jwtTokenProvider.generateTokens(
            userId,
            "test@zimdugo.com",
            "test-sid",
            1L
        );

        refreshTokenRepository.save(
            userId,
            tokens.sid(),
            tokens.refreshJti(),
            tokens.refreshToken(),
            Duration.ofDays(30)
        );

        assertThat(
            refreshTokenRepository.matches(userId, tokens.sid(), tokens.refreshToken())
        ).isTrue();

        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .cookie(new Cookie("refreshToken", tokens.refreshToken())))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    @DisplayName("logout 요청 시 200과 만료 쿠키를 반환한다")
    void logout_returns200AndExpiresCookie() throws Exception {
        User savedUser = userJpaRepository.save(
            new User(
                "logout@zimdugo.com",
                "로그아웃유저",
                null,
                UserStatus.ACTIVE
            )
        );

        Long userId = extractUserId(savedUser);
        assertThat(userId).isNotNull();

        AuthTokens tokens = jwtTokenProvider.generateTokens(
            userId,
            "logout@zimdugo.com",
            "logout-sid",
            1L
        );

        refreshTokenRepository.save(
            userId,
            tokens.sid(),
            tokens.refreshJti(),
            tokens.refreshToken(),
            Duration.ofDays(30)
        );

        assertThat(
            refreshTokenRepository.matches(userId, tokens.sid(), tokens.refreshToken())
        ).isTrue();

        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .header("Authorization", "Bearer " + tokens.accessToken()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists("Set-Cookie"));

        assertThat(
            refreshTokenRepository.matches(userId, tokens.sid(), tokens.refreshToken())
        ).isFalse();
    }

    private Long extractUserId(User user) {
        try {
            Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            Object value = field.get(user);
            return (Long) value;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("User id 추출 실패", e);
        }
    }
}
