package com.zimdugo.locker.entrypoint;

import com.zimdugo.auth.entrypoint.JwtAuthenticationFilter;
import com.zimdugo.auth.entrypoint.OAuth2CallbackUrlCaptureFilter;
import com.zimdugo.common.config.SecurityConfig;
import com.zimdugo.locker.application.LockerReportCommandService;
import com.zimdugo.locker.application.LockerReportCreateResult;
import com.zimdugo.locker.application.LockerReportDuplicateCandidateResponse;
import com.zimdugo.locker.application.LockerReportDuplicateQueryService;
import com.zimdugo.locker.application.LockerReportDuplicateResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private LockerReportDuplicateQueryService lockerReportDuplicateQueryService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private OAuth2CallbackUrlCaptureFilter oAuth2CallbackUrlCaptureFilter;

    @Test
    @DisplayName("중복 후보 조회 요청이 성공하면 반경 내 기존 보관함을 반환한다")
    void findDuplicateLockerCandidatesReturnsOk() throws Exception {
        given(lockerReportDuplicateQueryService.findDuplicates(37.556, 126.923, 30))
            .willReturn(LockerReportDuplicateResponse.of(30, List.of(
                new LockerReportDuplicateCandidateResponse(
                    12L,
                    "신촌역 물품보관함",
                    "서울 마포구 양화로 160",
                    37.556,
                    126.923,
                    8.4
                )
            )));

        mockMvc.perform(get("/api/v1/locker-reports/duplicates")
                .principal(authenticatedUser())
                .param("lat", "37.556")
                .param("lng", "126.923"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200"))
            .andExpect(jsonPath("$.data.hasDuplicates").value(true))
            .andExpect(jsonPath("$.data.radiusMeters").value(30))
            .andExpect(jsonPath("$.data.candidates[0].lockerId").value(12))
            .andExpect(jsonPath("$.data.candidates[0].distanceMeters").value(8.4));
    }

    @Test
    @DisplayName("인증 정보 없이 중복 후보를 조회하면 401을 반환한다")
    void findDuplicateLockerCandidatesWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/locker-reports/duplicates")
                .param("lat", "37.556")
                .param("lng", "126.923"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("A4011"))
            .andExpect(jsonPath("$.message").value("auth.authenticated_user_not_found"));
    }

    @Test
    @DisplayName("위도 정보 없이 중복 후보를 조회하면 bad request를 반환한다")
    void findDuplicateLockerCandidatesWithoutLatitudeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/locker-reports/duplicates")
                .param("lng", "126.923"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("위도를 숫자로 보내지 않으면 bad request를 반환한다")
    void findDuplicateLockerCandidatesWithInvalidLatitudeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/locker-reports/duplicates")
                .param("lat", "abc")
                .param("lng", "126.923"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C400"))
            .andExpect(jsonPath("$.message").value("common.bad_request"));
    }

    @Test
    @DisplayName("잘못된 JSON으로 제보를 등록하면 bad request를 반환한다")
    void createLockerReportWithMalformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/locker-reports")
                .principal(authenticatedUser())
                .contentType("application/json")
                .content("{roadAddress:\"서울 마포구 양화로 160\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C400"))
            .andExpect(jsonPath("$.message").value("common.bad_request"));
    }

    @Test
    @DisplayName("인증된 사용자가 제보를 등록하면 성공 응답을 반환한다")
    void createLockerReportWithAuthenticatedUserReturnsOk() throws Exception {
        given(lockerReportCommandService.create(eq(1L), any()))
            .willReturn(new LockerReportCreateResult(
                100L,
                10L,
                "신촌역 2번 출구 물품보관함",
                "서울 마포구 양화로 160",
                37.556,
                126.923,
                "COMPLETED"
            ));

        mockMvc.perform(post("/api/v1/locker-reports")
                .principal(authenticatedUser())
                .contentType("application/json")
                .content(validCreateRequestJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200"))
            .andExpect(jsonPath("$.message").value("common.ok"))
            .andExpect(jsonPath("$.data.reportId").value(100))
            .andExpect(jsonPath("$.data.lockerId").value(10))
            .andExpect(jsonPath("$.data.reportStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("인증 정보가 없으면 401을 반환한다")
    void createLockerReportWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/locker-reports")
                .contentType("application/json")
                .content(validCreateRequestJson()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("A4011"))
            .andExpect(jsonPath("$.message").value("auth.authenticated_user_not_found"));
    }

    @Test
    @DisplayName("보관함 유형이 비어 있으면 validation error를 반환한다")
    void createLockerReportWithoutLockerTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/locker-reports")
                .principal(authenticatedUser())
                .contentType("application/json")
                .content("""
                    {
                      "roadAddress": "서울 마포구 양화로 160",
                      "latitude": 37.556,
                      "longitude": 126.923,
                      "hasFloor": false,
                      "floorType": null,
                      "floorNumber": null,
                      "indoorOutdoorType": "INDOOR",
                      "lockerType": "",
                      "sizeTypes": ["SMALL", "MEDIUM"],
                      "isFree": true,
                      "minPrice": null,
                      "maxPrice": null,
                      "startTime": null,
                      "endTime": null,
                      "additionalInfo": "신촌역 2번 출구 근처",
                      "imageUrl": null,
                      "locationConsentAgreed": true
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C400"))
            .andExpect(jsonPath("$.validationErrors[0].field").value("lockerType"))
            .andExpect(jsonPath("$.validationErrors[0].message").value("validation.not_blank"));
    }

    @Test
    @DisplayName("도로명 주소가 비어 있으면 validation error를 반환한다")
    void createLockerReportWithoutRoadAddressReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/locker-reports")
                .principal(authenticatedUser())
                .contentType("application/json")
                .content("""
                    {
                      "roadAddress": "",
                      "latitude": 37.556,
                      "longitude": 126.923,
                      "hasFloor": false,
                      "floorType": null,
                      "floorNumber": null,
                      "indoorOutdoorType": "INDOOR",
                      "lockerType": "SUBWAY_STATION",
                      "sizeTypes": ["SMALL", "MEDIUM"],
                      "isFree": true,
                      "minPrice": null,
                      "maxPrice": null,
                      "startTime": null,
                      "endTime": null,
                      "additionalInfo": "신촌역 2번 출구 근처",
                      "imageUrl": null,
                      "locationConsentAgreed": true
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C400"))
            .andExpect(jsonPath("$.validationErrors[0].field").value("roadAddress"))
            .andExpect(jsonPath("$.validationErrors[0].message").value("validation.not_blank"));
    }

    private String validCreateRequestJson() {
        return """
            {
              "roadAddress": "서울 마포구 양화로 160",
              "latitude": 37.556,
              "longitude": 126.923,
              "hasFloor": false,
              "floorType": null,
              "floorNumber": null,
              "indoorOutdoorType": "INDOOR",
              "lockerType": "SUBWAY_STATION",
              "sizeTypes": ["SMALL", "MEDIUM"],
              "isFree": true,
              "minPrice": null,
              "maxPrice": null,
              "startTime": null,
              "endTime": null,
              "additionalInfo": "신촌역 2번 출구 근처",
              "imageUrl": null,
              "locationConsentAgreed": true
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
