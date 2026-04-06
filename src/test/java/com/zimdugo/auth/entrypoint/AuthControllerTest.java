package com.zimdugo.auth.entrypoint;

import com.zimdugo.auth.application.AccountWithdrawalService;
import com.zimdugo.auth.application.AuthCommandService;
import com.zimdugo.auth.application.AuthRefreshResult;
import com.zimdugo.common.config.SecurityConfig;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = SecurityConfig.class
    ),
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
    private AuthCommandService authCommandService;

    @MockitoBean
    private AccountWithdrawalService accountWithdrawalService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("유효한 RT로 리프레시 요청 시 200을 반환한다")
    void refresh_withValidRT_returns200() throws Exception {
        given(authCommandService.refresh("valid-rt"))
            .willReturn(new AuthRefreshResult(1L, "test@zimdugo.com", "new-at", "new-rt"));

        mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", "valid-rt")))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RT가 없으면 4xx를 반환한다")
    void refresh_withoutRT_returns4xx() throws Exception {
        given(authCommandService.refresh(null))
            .willThrow(new IllegalArgumentException("refresh token not found"));

        mockMvc.perform(post("/api/auth/refresh"))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("RT가 불일치하면 4xx를 반환한다")
    void refresh_withMismatchedRT_returns4xx() throws Exception {
        given(authCommandService.refresh("invalid-rt"))
            .willThrow(new IllegalArgumentException("refresh token mismatch"));

        mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", "invalid-rt")))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그아웃은 200을 반환하고 만료 쿠키 헤더를 포함한다")
    void logout_returns200AndExpiresCookie() throws Exception {
        doNothing().when(authCommandService).logout(null, "valid-at");

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer valid-at"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("Authorization 헤더가 없어도 200을 반환하고 만료 쿠키 헤더를 포함한다")
    void logout_withoutAuthorization_returns200AndExpiresCookie() throws Exception {
        doNothing().when(authCommandService).logout(null, null);

        mockMvc.perform(post("/api/auth/logout"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("탈퇴 요청 시 200과 만료 쿠키를 반환한다")
    void withdraw_returns200AndExpiresCookie() throws Exception {
        doNothing().when(accountWithdrawalService).withdraw("valid-at");

        mockMvc.perform(post("/api/auth/withdraw")
                .header("Authorization", "Bearer valid-at"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists("Set-Cookie"));
    }
}
