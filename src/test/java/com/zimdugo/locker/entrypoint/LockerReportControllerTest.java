package com.zimdugo.locker.entrypoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zimdugo.auth.config.SecurityConfig;
import com.zimdugo.auth.entrypoint.JwtAuthenticationFilter;
import com.zimdugo.auth.entrypoint.OAuth2CallbackUrlCaptureFilter;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.LockerReportCommandService;
import com.zimdugo.locker.application.result.report.LockerReportCreateResult;
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
    @DisplayName("잘못된 JSON으로 제보를 등록하면 bad request를 반환한다")
    void createLockerReportWithMalformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/locker-reports")
                .principal(authenticatedUser())
                .contentType("application/json")
                .content("{roadAddress:\"서울 마포구 양화로 160\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.code").value("COMMON-400-2"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/v1/locker-reports"));
    }

    @Test
    @DisplayName("인증된 사용자가 제보를 등록하면 성공 응답을 반환한다")
    void createLockerReportWithAuthenticatedUserReturnsOk() throws Exception {
        given(lockerReportCommandService.create(eq(1L), any()))
            .willReturn(new LockerReportCreateResult(
                100L,
                "홍대입구역",
                "서울 마포구 양화로 160",
                37.556,
                126.923,
                "SUBMITTED"
            ));

        mockMvc.perform(post("/api/v1/locker-reports")
                .principal(authenticatedUser())
                .contentType("application/json")
                .content(validCreateRequestJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200"))
            .andExpect(jsonPath("$.message").value("common.ok"))
            .andExpect(jsonPath("$.data.reportId").value(100))
            .andExpect(jsonPath("$.data.reportStatus").value("SUBMITTED"));
    }

    @Test
    @DisplayName("인증 정보가 없으면 401을 반환한다")
    void createLockerReportWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/locker-reports")
                .contentType("application/json")
                .content(validCreateRequestJson()))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.code").value("AUTH-401-1"))
            .andExpect(jsonPath("$.message").value(ErrorCode.AUTHENTICATED_USER_NOT_FOUND.getMessage()))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.path").value("/api/v1/locker-reports"));
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
                      "is24Hours": false,
                      "startTime": null,
                      "endTime": null,
                      "additionalInfo": "2번 출구 근처",
                      "imageUrl": null,
                      "locationConsentAgreed": true
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.code").value("COMMON-400-1"))
            .andExpect(jsonPath("$.validationErrors").isArray())
            .andExpect(jsonPath("$.validationErrors[0].field").exists());
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
                      "is24Hours": false,
                      "startTime": null,
                      "endTime": null,
                      "additionalInfo": "2번 출구 근처",
                      "imageUrl": null,
                      "locationConsentAgreed": true
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.code").value("COMMON-400-1"))
            .andExpect(jsonPath("$.validationErrors").isArray())
            .andExpect(jsonPath("$.validationErrors[0].field").exists());
    }

    @Test
    @DisplayName("층 있음과 실외를 함께 보내면 validation error를 반환한다")
    void createLockerReportWithOutdoorFloorCombinationReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/locker-reports")
                .principal(authenticatedUser())
                .contentType("application/json")
                .content("""
                    {
                      "roadAddress": "서울 마포구 양화로 160",
                      "latitude": 37.556,
                      "longitude": 126.923,
                      "hasFloor": true,
                      "floorType": "UNDERGROUND",
                      "floorNumber": 2,
                      "indoorOutdoorType": "OUTDOOR",
                      "lockerType": "SUBWAY_STATION",
                      "sizeTypes": ["SMALL"],
                      "isFree": true,
                      "minPrice": null,
                      "maxPrice": null,
                      "is24Hours": false,
                      "startTime": null,
                      "endTime": null,
                      "additionalInfo": "야외 보관함",
                      "imageUrl": null,
                      "locationConsentAgreed": true
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON-400-1"));
    }

    @Test
    @DisplayName("24시간 운영인데 시간을 함께 보내면 validation error를 반환한다")
    void createLockerReportWithTwentyFourHourAndTimesReturnsBadRequest() throws Exception {
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
                      "lockerType": "SUBWAY_STATION",
                      "sizeTypes": ["SMALL"],
                      "isFree": true,
                      "minPrice": null,
                      "maxPrice": null,
                      "is24Hours": true,
                      "startTime": "09:00",
                      "endTime": "22:00",
                      "additionalInfo": "24시간",
                      "imageUrl": null,
                      "locationConsentAgreed": true
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON-400-1"));
    }

    @Test
    @DisplayName("보관함 사이즈가 비어 있으면 validation error를 반환한다")
    void createLockerReportWithoutSizeTypesReturnsBadRequest() throws Exception {
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
                      "lockerType": "SUBWAY_STATION",
                      "sizeTypes": [],
                      "isFree": true,
                      "minPrice": null,
                      "maxPrice": null,
                      "is24Hours": false,
                      "startTime": null,
                      "endTime": null,
                      "additionalInfo": "2번 출구 근처",
                      "imageUrl": null,
                      "locationConsentAgreed": true
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON-400-1"));
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
              "is24Hours": false,
              "startTime": null,
              "endTime": null,
              "additionalInfo": "2번 출구 근처",
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
