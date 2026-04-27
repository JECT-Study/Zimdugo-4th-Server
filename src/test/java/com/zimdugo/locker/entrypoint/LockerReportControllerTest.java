package com.zimdugo.locker.entrypoint;

import com.zimdugo.auth.entrypoint.JwtAuthenticationFilter;
import com.zimdugo.auth.entrypoint.OAuth2CallbackUrlCaptureFilter;
import com.zimdugo.common.config.SecurityConfig;
import com.zimdugo.locker.application.LockerReportCommandService;
import com.zimdugo.locker.application.LockerReportCreateResult;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = LockerReportController.class,
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
class LockerReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LockerReportCommandService lockerReportCommandService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private OAuth2CallbackUrlCaptureFilter oAuth2CallbackUrlCaptureFilter;

    @Test
    @DisplayName("인증된 사용자 요청이면 제보 등록 응답을 반환한다")
    void createLockerReport_withAuthenticatedUser_returnsOk() throws Exception {
        given(lockerReportCommandService.create(eq(1L), any()))
            .willReturn(new LockerReportCreateResult(
                100L,
                10L,
                "Hongdae Exit 2 Test Locker",
                null,
                37.556,
                126.923,
                "COMPLETED"
            ));

        mockMvc.perform(post("/api/v1/locker-reports")
                .principal(authenticatedUser())
                .contentType("application/json")
                .content(validCreateNewRequestJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200"))
            .andExpect(jsonPath("$.message").value("common.ok"))
            .andExpect(jsonPath("$.data.reportId").value(100))
            .andExpect(jsonPath("$.data.lockerId").value(10))
            .andExpect(jsonPath("$.data.reportStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("인증 정보가 없으면 401을 반환한다")
    void createLockerReport_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/locker-reports")
                .contentType("application/json")
                .content(validCreateNewRequestJson()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("A4011"))
            .andExpect(jsonPath("$.message").value("auth.authenticated_user_not_found"));
    }

    @Test
    @DisplayName("필수값이 누락되면 400과 validation error를 반환한다")
    void createLockerReport_withoutName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/locker-reports")
                .principal(authenticatedUser())
                .contentType("application/json")
                .content("""
                    {
                      "duplicateHandlingType": "CREATE_NEW",
                      "existingLockerId": null,
                      "name": "",
                      "roadAddress": null,
                      "detailLocation": null,
                      "buildingName": "Hongdae Station",
                      "floor": null,
                      "indoorOutdoorType": null,
                      "lockerType": null,
                      "sizeInfo": null,
                      "priceInfo": null,
                      "operatingHours": null,
                      "imageUrl": null,
                      "latitude": 37.556,
                      "longitude": 126.923
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C400"))
            .andExpect(jsonPath("$.validationErrors[0].field").value("name"))
            .andExpect(jsonPath("$.validationErrors[0].message").value("validation.not_blank"));
    }

    @Test
    @DisplayName("기존 장소 추가인데 보관함 ID가 없으면 400을 반환한다")
    void createLockerReport_addToExistingWithoutLockerId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/locker-reports")
                .principal(authenticatedUser())
                .contentType("application/json")
                .content("""
                    {
                      "duplicateHandlingType": "ADD_TO_EXISTING",
                      "existingLockerId": null,
                      "name": "Hongdae Exit 2 Test Locker",
                      "roadAddress": null,
                      "detailLocation": null,
                      "buildingName": "Hongdae Station",
                      "floor": null,
                      "indoorOutdoorType": null,
                      "lockerType": null,
                      "sizeInfo": null,
                      "priceInfo": null,
                      "operatingHours": null,
                      "imageUrl": null,
                      "latitude": 37.556,
                      "longitude": 126.923
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C400"))
            .andExpect(jsonPath("$.message").value("common.bad_request"));
    }

    private String validCreateNewRequestJson() {
        return """
            {
              "duplicateHandlingType": "CREATE_NEW",
              "existingLockerId": null,
              "name": "Hongdae Exit 2 Test Locker",
              "roadAddress": null,
              "detailLocation": null,
              "buildingName": "Hongdae Station",
              "floor": null,
              "indoorOutdoorType": null,
              "lockerType": null,
              "sizeInfo": null,
              "priceInfo": null,
              "operatingHours": null,
              "imageUrl": null,
              "latitude": 37.556,
              "longitude": 126.923
            }
            """;
    }

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken(
            "1",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
