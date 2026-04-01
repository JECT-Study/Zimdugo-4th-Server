package com.zimdugo.auth.entrypoint;

import com.zimdugo.auth.application.JwtTokenProvider;
import com.zimdugo.auth.domain.AuthTokens;
import com.zimdugo.auth.domain.RefreshTokenRepository;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.infrastructure.UserJpaRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserJpaRepository userJpaRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("유효한 RT로 재발급 요청 시 200을 반환한다")
    void refresh_withValidRT_returns200() throws Exception {
        given(jwtTokenProvider.validateToken("valid-rt")).willReturn(true);
        given(jwtTokenProvider.getUserId("valid-rt")).willReturn(1L);
        given(jwtTokenProvider.getSid("valid-rt")).willReturn("sid");
        given(jwtTokenProvider.getUv("valid-rt")).willReturn(1L);

        User user = new User(
            "test@zimdugo.com",
            "테스트",
            null,
            UserStatus.ACTIVE
        );
        setUserId(user, 1L);

        given(userJpaRepository.findById(1L)).willReturn(Optional.of(user));
        given(refreshTokenRepository.matches(1L, "sid", "valid-rt")).willReturn(true);

        given(jwtTokenProvider.generateTokens(
            nullable(Long.class),
            nullable(String.class),
            nullable(String.class),
            anyLong()
        )).willReturn(new AuthTokens("new-at", "new-rt", "new-sid", "new-jti"));

        doNothing().when(refreshTokenRepository).save(
            anyLong(),
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            any()
        );

        mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", "valid-rt")))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RT가 없으면 4xx를 반환한다")
    void refresh_withoutRT_returns4xx() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("저장된 RT와 불일치하면 4xx를 반환한다")
    void refresh_withMismatchedRT_returns4xx() throws Exception {
        given(jwtTokenProvider.validateToken("invalid-rt")).willReturn(true);
        given(jwtTokenProvider.getUserId("invalid-rt")).willReturn(1L);
        given(jwtTokenProvider.getSid("invalid-rt")).willReturn("sid");

        User user = new User(
            "test@zimdugo.com",
            "테스트",
            null,
            UserStatus.ACTIVE
        );
        setUserId(user, 1L);

        given(userJpaRepository.findById(1L)).willReturn(Optional.of(user));
        given(refreshTokenRepository.matches(1L, "sid", "invalid-rt")).willReturn(false);

        mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", "invalid-rt")))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그아웃 시 200을 반환하고 쿠키 만료 헤더를 포함한다")
    void logout_returns200AndExpiresCookie() throws Exception {
        given(jwtTokenProvider.validateToken("valid-at")).willReturn(true);
        given(jwtTokenProvider.getUserId("valid-at")).willReturn(1L);
        given(jwtTokenProvider.getSid("valid-at")).willReturn("sid");

        doNothing().when(refreshTokenRepository).delete(1L, "sid");

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer valid-at"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("로그아웃 요청에 Authorization 헤더가 없어도 200을 반환하고 쿠키 만료 헤더를 포함한다")
    void logout_withoutAuthorization_returns200AndExpiresCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists("Set-Cookie"));
    }

    private void setUserId(User user, Long id) {
        try {
            Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (NoSuchFieldException e) {
            // ignore
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
