package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.application.AdminDocumentService;
import com.zimdugo.common.i18n.AcceptLanguageResolver;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.auth.config.SecurityConfig;
import com.zimdugo.auth.entrypoint.filter.JwtAuthenticationFilter;
import com.zimdugo.auth.entrypoint.filter.OAuth2CallbackUrlCaptureFilter;
import java.util.List;
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
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = ClientDocumentController.class,
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
class ClientDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminDocumentService adminDocumentService;

    @MockitoBean
    private AcceptLanguageResolver acceptLanguageResolver;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private OAuth2CallbackUrlCaptureFilter oAuth2CallbackUrlCaptureFilter;

    @Test
    @DisplayName("Accept-Language에서 가장 선호하는 언어를 문서 조회 서비스에 전달한다")
    void passesPreferredAcceptLanguage() throws Exception {
        given(acceptLanguageResolver.resolve("en;q=0.5, ko-KR;q=0.9")).willReturn(SupportedLanguage.KOREAN);
        given(adminDocumentService.getLocalizedActiveDocumentsByType(
            eq("NOTICE"),
            eq(SupportedLanguage.KOREAN)
        ))
            .willReturn(List.of());

        mockMvc.perform(get("/api/v1/documents")
                .param("type", "NOTICE")
                .header(HttpHeaders.ACCEPT_LANGUAGE, "en;q=0.5, ko-KR;q=0.9"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200"))
            .andExpect(jsonPath("$.data").isArray());

        verify(adminDocumentService).getLocalizedActiveDocumentsByType("NOTICE", SupportedLanguage.KOREAN);
    }

    @Test
    @DisplayName("Accept-Language가 없으면 기존 원문 언어인 한국어 문서를 조회한다")
    void defaultsToKorean() throws Exception {
        given(acceptLanguageResolver.resolve(null)).willReturn(SupportedLanguage.KOREAN);
        given(adminDocumentService.getLocalizedActiveDocumentsByType(
            eq("NOTICE"),
            eq(SupportedLanguage.KOREAN)
        ))
            .willReturn(List.of());

        mockMvc.perform(get("/api/v1/documents").param("type", "NOTICE"))
            .andExpect(status().isOk());

        verify(adminDocumentService).getLocalizedActiveDocumentsByType("NOTICE", SupportedLanguage.KOREAN);
    }
}
