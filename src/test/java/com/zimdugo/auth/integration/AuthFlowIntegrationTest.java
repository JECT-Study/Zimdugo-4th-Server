package com.zimdugo.auth.integration;

import com.zimdugo.auth.application.JwtTokenProvider;
import com.zimdugo.auth.domain.AuthTokens;
import com.zimdugo.auth.domain.RefreshTokenRepository;
import com.zimdugo.identity.domain.AuthProvider;
import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.SocialAccountStore;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserReader;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.domain.UserStore;
import com.zimdugo.user.infrastructure.SocialAccountJpaRepository;
import com.zimdugo.user.infrastructure.UserJpaRepository;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AuthFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("zimdugo_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis =
        new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
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
    private SocialAccountJpaRepository socialAccountJpaRepository;

    @Autowired
    private UserStore userStore;

    @Autowired
    private UserReader userReader;

    @Autowired
    private SocialAccountStore socialAccountStore;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        stringRedisTemplate.getConnectionFactory()
            .getConnection()
            .serverCommands()
            .flushAll();

        socialAccountJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("refresh with valid RT returns new access token")
    void refresh_withValidRefreshToken_returnsNewAccessToken() throws Exception {
        User savedUser = userStore.store(
            new User("test@zimdugo.com", "test", null, UserStatus.ACTIVE)
        );
        Long userId = savedUser.getId();
        assertThat(userId).isNotNull();

        AuthTokens tokens = jwtTokenProvider.generateTokens(
            userId,
            "test@zimdugo.com",
            "USER",
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

        assertThat(refreshTokenRepository.matches(userId, tokens.sid(), tokens.refreshToken())).isTrue();

        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .cookie(new Cookie("refreshToken", tokens.refreshToken())))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    @DisplayName("logout returns 200 and expires cookie")
    void logout_returns200AndExpiresCookie() throws Exception {
        User savedUser = userStore.store(
            new User("logout@zimdugo.com", "logout-user", null, UserStatus.ACTIVE)
        );
        Long userId = savedUser.getId();
        assertThat(userId).isNotNull();

        AuthTokens tokens = jwtTokenProvider.generateTokens(
            userId,
            "logout@zimdugo.com",
            "USER",
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

        assertThat(refreshTokenRepository.matches(userId, tokens.sid(), tokens.refreshToken())).isTrue();

        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .header("Authorization", "Bearer " + tokens.accessToken()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists("Set-Cookie"));

        assertThat(refreshTokenRepository.matches(userId, tokens.sid(), tokens.refreshToken())).isFalse();
    }

    @Test
    @DisplayName("logout from one session invalidates all sessions and old AT")
    void logout_invalidatesAllSessionsAndAccessTokens() throws Exception {
        User savedUser = userStore.store(
            new User("multi@zimdugo.com", "multi-session", null, UserStatus.ACTIVE)
        );
        Long userId = savedUser.getId();
        assertThat(userId).isNotNull();

        AuthTokens session1 = jwtTokenProvider.generateTokens(
            userId,
            "multi@zimdugo.com",
            "USER",
            "sid-1",
            1L
        );
        AuthTokens session2 = jwtTokenProvider.generateTokens(
            userId,
            "multi@zimdugo.com",
            "USER",
            "sid-2",
            1L
        );

        refreshTokenRepository.save(
            userId,
            session1.sid(),
            session1.refreshJti(),
            session1.refreshToken(),
            Duration.ofDays(30)
        );
        refreshTokenRepository.save(
            userId,
            session2.sid(),
            session2.refreshJti(),
            session2.refreshToken(),
            Duration.ofDays(30)
        );

        mockMvc.perform(get("/api/v1/me")
                .header("Authorization", "Bearer " + session2.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .header("Authorization", "Bearer " + session1.accessToken()))
            .andExpect(status().isOk());

        assertThat(refreshTokenRepository.matches(userId, session1.sid(), session1.refreshToken())).isFalse();
        assertThat(refreshTokenRepository.matches(userId, session2.sid(), session2.refreshToken())).isFalse();

        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .cookie(new Cookie("refreshToken", session2.refreshToken())))
            .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/api/v1/me")
                .header("Authorization", "Bearer " + session2.accessToken()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("withdraw deactivates user, deletes social links, and revokes tokens")
    void withdraw_deletesSessionAndDeactivatesUser() throws Exception {
        User savedUser = userStore.store(
            new User("withdraw@zimdugo.com", "withdraw-user", null, UserStatus.ACTIVE)
        );
        Long userId = savedUser.getId();
        assertThat(userId).isNotNull();

        socialAccountStore.store(
            new SocialAccount(
                savedUser,
                AuthProvider.KAKAO,
                "withdraw-provider-id",
                null,
                null
            )
        );

        AuthTokens tokens = jwtTokenProvider.generateTokens(
            userId,
            "withdraw@zimdugo.com",
            "USER",
            "withdraw-sid",
            1L
        );

        refreshTokenRepository.save(
            userId,
            tokens.sid(),
            tokens.refreshJti(),
            tokens.refreshToken(),
            Duration.ofDays(30)
        );

        mockMvc.perform(post("/api/auth/withdraw")
                .with(csrf())
                .header("Authorization", "Bearer " + tokens.accessToken()))
            .andExpect(status().isOk())
            .andExpect(header().exists("Set-Cookie"));

        User withdrawnUser = userReader.findById(userId).orElseThrow();
        assertThat(withdrawnUser.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(socialAccountJpaRepository.findAllByUserId(userId)).isEmpty();
        assertThat(refreshTokenRepository.matches(userId, tokens.sid(), tokens.refreshToken())).isFalse();

        mockMvc.perform(get("/api/v1/me")
                .header("Authorization", "Bearer " + tokens.accessToken()))
            .andExpect(status().isUnauthorized());
    }
}
