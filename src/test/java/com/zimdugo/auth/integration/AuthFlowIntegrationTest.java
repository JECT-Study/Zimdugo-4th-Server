package com.zimdugo.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zimdugo.auth.application.JwtTokenProvider;
import com.zimdugo.auth.domain.AuthTokens;
import com.zimdugo.auth.domain.RefreshTokenRepository;
import com.zimdugo.user.domain.AuthProvider;
import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserStatus;
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
        User savedUser = userJpaRepository.save(
            new User("test@zimdugo.com", "test", null, UserStatus.ACTIVE)
        );

        AuthTokens tokens = jwtTokenProvider.generateTokens(
            savedUser.getId(),
            "test@zimdugo.com",
            "USER",
            "test-sid"
        );

        refreshTokenRepository.save(
            savedUser.getId(),
            tokens.sid(),
            tokens.refreshToken(),
            Duration.ofDays(30)
        );

        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .cookie(new Cookie("refreshToken", tokens.refreshToken())))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    @DisplayName("logout invalidates only current session")
    void logout_invalidatesOnlyCurrentSession() throws Exception {
        User savedUser = userJpaRepository.save(
            new User("multi@zimdugo.com", "multi", null, UserStatus.ACTIVE)
        );

        AuthTokens session1 = jwtTokenProvider.generateTokens(
            savedUser.getId(),
            "multi@zimdugo.com",
            "USER",
            "sid-1"
        );
        AuthTokens session2 = jwtTokenProvider.generateTokens(
            savedUser.getId(),
            "multi@zimdugo.com",
            "USER",
            "sid-2"
        );

        refreshTokenRepository.save(
            savedUser.getId(),
            session1.sid(),
            session1.refreshToken(),
            Duration.ofDays(30)
        );
        refreshTokenRepository.save(
            savedUser.getId(),
            session2.sid(),
            session2.refreshToken(),
            Duration.ofDays(30)
        );

        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .header("Authorization", "Bearer " + session1.accessToken()))
            .andExpect(status().isOk())
            .andExpect(header().exists("Set-Cookie"));

        assertThat(refreshTokenRepository.matches(savedUser.getId(), session1.sid(), session1.refreshToken()))
            .isFalse();
        assertThat(refreshTokenRepository.matches(savedUser.getId(), session2.sid(), session2.refreshToken()))
            .isTrue();
    }

    @Test
    @DisplayName("withdraw sets user status to deleted and revokes refresh token")
    void withdraw_deletesUserAndRefreshTokens() throws Exception {
        User savedUser = userJpaRepository.save(
            new User("withdraw@zimdugo.com", "withdraw", null, UserStatus.ACTIVE)
        );
        socialAccountJpaRepository.save(
            new SocialAccount(savedUser, AuthProvider.KAKAO, "provider-user-id", null, null)
        );

        AuthTokens tokens = jwtTokenProvider.generateTokens(
            savedUser.getId(),
            "withdraw@zimdugo.com",
            "USER",
            "withdraw-sid"
        );

        refreshTokenRepository.save(
            savedUser.getId(),
            tokens.sid(),
            tokens.refreshToken(),
            Duration.ofDays(30)
        );

        mockMvc.perform(post("/api/auth/withdraw")
                .with(csrf())
                .header("Authorization", "Bearer " + tokens.accessToken()))
            .andExpect(status().isOk());

        User withdrawnUser = userJpaRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(withdrawnUser.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(socialAccountJpaRepository.findAllByUserId(savedUser.getId())).isEmpty();
        assertThat(refreshTokenRepository.matches(savedUser.getId(), tokens.sid(), tokens.refreshToken())).isFalse();

        mockMvc.perform(get("/api/v1/me")
                .header("Authorization", "Bearer " + tokens.accessToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DELETED"));
    }
}
