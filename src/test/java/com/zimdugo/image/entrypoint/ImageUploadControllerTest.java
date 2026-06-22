package com.zimdugo.image.entrypoint;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zimdugo.auth.config.SecurityConfig;
import com.zimdugo.auth.entrypoint.filter.JwtAuthenticationFilter;
import com.zimdugo.auth.entrypoint.filter.OAuth2CallbackUrlCaptureFilter;
import com.zimdugo.image.application.PresignedImageUploadService;
import com.zimdugo.image.application.PresignedUploadResult;
import com.zimdugo.image.application.UploadCategory;
import java.time.Instant;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = ImageUploadController.class,
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
class ImageUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PresignedImageUploadService presignedImageUploadService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private OAuth2CallbackUrlCaptureFilter oAuth2CallbackUrlCaptureFilter;

    @Test
    @DisplayName("업로드 메타데이터 표준 필드로 presigned URL을 발급한다")
    void createPresignedUploadWithStandardRequestReturnsOk() throws Exception {
        given(presignedImageUploadService.createPresignedUpload(
            eq(UploadCategory.LOCKER_REPORT),
            eq("locker-photo.jpg"),
            eq("image/jpeg"),
            eq(12345L),
            eq(1L)
        )).willReturn(result());

        mockMvc.perform(post("/api/v1/uploads")
                .principal(authenticatedUser())
                .contentType("application/json")
                .content("""
                    {
                      "category": "LOCKER_REPORT",
                      "fileName": "locker-photo.jpg",
                      "contentType": "image/jpeg",
                      "contentLength": 12345
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.uploadUrl").value("https://s3.example.com/upload"))
            .andExpect(jsonPath("$.data.fileUrl").value("https://cdn.example.com/reports/locker-photo.jpg"));
    }

    private PresignedUploadResult result() {
        return new PresignedUploadResult(
            "https://s3.example.com/upload",
            "https://cdn.example.com/reports/locker-photo.jpg",
            "reports/locker-photo.jpg",
            Instant.parse("2026-06-21T00:00:00Z")
        );
    }

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(
            "1",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
