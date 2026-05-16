package com.zimdugo.locker.entrypoint;

import com.zimdugo.auth.entrypoint.JwtAuthenticationFilter;
import com.zimdugo.auth.entrypoint.OAuth2CallbackUrlCaptureFilter;
import com.zimdugo.common.config.SecurityConfig;
import com.zimdugo.locker.application.FavoriteLockerCommandService;
import com.zimdugo.locker.application.FavoriteLockerItemResponse;
import com.zimdugo.locker.application.FavoriteLockerQueryService;
import com.zimdugo.locker.application.FavoriteLockerResponse;
import com.zimdugo.locker.application.FavoriteLockerStatusResponse;
import java.time.LocalDateTime;
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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = LockerFavoriteController.class,
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
class LockerFavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoriteLockerQueryService favoriteLockerQueryService;

    @MockitoBean
    private FavoriteLockerCommandService favoriteLockerCommandService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private OAuth2CallbackUrlCaptureFilter oAuth2CallbackUrlCaptureFilter;

    @Test
    @DisplayName("인증된 사용자의 즐겨찾기 보관함 목록을 최신순 페이지로 조회한다")
    void getMyFavoriteLockersReturnsOk() throws Exception {
        given(favoriteLockerQueryService.getFavorites(1L, 0, 20, 37.555, 126.922))
            .willReturn(new FavoriteLockerResponse(
                11,
                0,
                20,
                false,
                List.of(new FavoriteLockerItemResponse(
                    10L,
                    "홍대입구역 보관함",
                    "서울 마포구 양화로 160",
                    37.556,
                    126.923,
                    LocalDateTime.of(2026, 5, 11, 10, 30),
                    LocalDateTime.of(2026, 5, 10, 18, 0),
                    120L
                ))
            ));

        mockMvc.perform(get("/api/v1/me/favorite-lockers")
                .param("lat", "37.555")
                .param("lng", "126.922")
                .principal(authenticatedUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200"))
            .andExpect(jsonPath("$.data.totalCount").value(11))
            .andExpect(jsonPath("$.data.items[0].lockerId").value(10))
            .andExpect(jsonPath("$.data.items[0].poiName").value("홍대입구역 보관함"))
            .andExpect(jsonPath("$.data.items[0].roadAddress").value("서울 마포구 양화로 160"))
            .andExpect(jsonPath("$.data.items[0].distanceMeters").value(120))
            .andExpect(jsonPath("$.data.items[0].lastCompletedVoteAt").exists());
    }

    @Test
    @DisplayName("인증 정보 없이 즐겨찾기 목록을 조회하면 401을 반환한다")
    void getMyFavoriteLockersWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/me/favorite-lockers"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("A4011"))
            .andExpect(jsonPath("$.message").value("auth.authenticated_user_not_found"));
    }

    @Test
    @DisplayName("숫자가 아닌 인증 이름이면 401을 반환한다")
    void getMyFavoriteLockersWithNonNumericAuthenticationNameReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/me/favorite-lockers")
                .principal(authenticationWithName("user-name")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("A4011"))
            .andExpect(jsonPath("$.message").value("auth.authenticated_user_not_found"));
    }

    @Test
    @DisplayName("특정 보관함의 즐겨찾기 상태를 조회한다")
    void getFavoriteLockerStatusReturnsOk() throws Exception {
        given(favoriteLockerQueryService.getFavoriteStatus(1L, 10L))
            .willReturn(new FavoriteLockerStatusResponse(10L, true));

        mockMvc.perform(get("/api/v1/me/favorite-lockers/{lockerId}/status", 10L)
                .principal(authenticatedUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200"))
            .andExpect(jsonPath("$.data.lockerId").value(10))
            .andExpect(jsonPath("$.data.favorite").value(true));
    }

    @Test
    @DisplayName("삭제된 보관함은 즐겨찾기 상태 조회에서 미등록으로 본다")
    void getFavoriteLockerStatusReturnsFalseForDeletedLocker() throws Exception {
        given(favoriteLockerQueryService.getFavoriteStatus(1L, 99L))
            .willReturn(new FavoriteLockerStatusResponse(99L, false));

        mockMvc.perform(get("/api/v1/me/favorite-lockers/{lockerId}/status", 99L)
                .principal(authenticatedUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.lockerId").value(99))
            .andExpect(jsonPath("$.data.favorite").value(false));
    }

    @Test
    @DisplayName("0 이하 lockerId로 상태 조회하면 400을 반환한다")
    void getFavoriteLockerStatusWithNonPositiveLockerIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/me/favorite-lockers/{lockerId}/status", 0L)
                .principal(authenticatedUser()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C400"));
    }

    @Test
    @DisplayName("인증 정보 없이 즐겨찾기 상태를 조회하면 401을 반환한다")
    void getFavoriteLockerStatusWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/me/favorite-lockers/{lockerId}/status", 10L))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("A4011"))
            .andExpect(jsonPath("$.message").value("auth.authenticated_user_not_found"));
    }

    @Test
    @DisplayName("즐겨찾기 순서를 변경한다")
    void reorderFavoriteLockersReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/v1/me/favorite-lockers/order")
                .principal(authenticatedUser())
                .contentType("application/json")
                .content("""
                    {
                      "lockerIds": [20, 10, 30]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200"));

        verify(favoriteLockerCommandService).reorder(1L, List.of(20L, 10L, 30L));
    }

    @Test
    @DisplayName("비어 있는 순서 변경 요청은 400을 반환한다")
    void reorderFavoriteLockersWithEmptyIdsReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/me/favorite-lockers/order")
                .principal(authenticatedUser())
                .contentType("application/json")
                .content("""
                    {
                      "lockerIds": []
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C400"));
    }

    @Test
    @DisplayName("음수 lockerId가 포함된 순서 변경 요청은 400을 반환한다")
    void reorderFavoriteLockersWithNegativeLockerIdReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/me/favorite-lockers/order")
                .principal(authenticatedUser())
                .contentType("application/json")
                .content("""
                    {
                      "lockerIds": [20, -1, 30]
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C400"));
    }

    @Test
    @DisplayName("보관함을 즐겨찾기로 등록한다")
    void addFavoriteLockerReturnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/me/favorite-lockers/{lockerId}", 10L)
                .principal(authenticatedUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200"));

        verify(favoriteLockerCommandService).add(1L, 10L);
    }

    @Test
    @DisplayName("0 이하 lockerId로 즐겨찾기 등록하면 400을 반환한다")
    void addFavoriteLockerWithNonPositiveLockerIdReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/me/favorite-lockers/{lockerId}", 0L)
                .principal(authenticatedUser()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C400"));
    }

    @Test
    @DisplayName("보관함 즐겨찾기를 해제한다")
    void removeFavoriteLockerReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/me/favorite-lockers/{lockerId}", 10L)
                .principal(authenticatedUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200"));

        verify(favoriteLockerCommandService).remove(1L, 10L);
    }

    @Test
    @DisplayName("0 이하 lockerId로 즐겨찾기 해제하면 400을 반환한다")
    void removeFavoriteLockerWithNonPositiveLockerIdReturnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/v1/me/favorite-lockers/{lockerId}", 0L)
                .principal(authenticatedUser()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("C400"));
    }

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        return authenticationWithName("1");
    }

    private UsernamePasswordAuthenticationToken authenticationWithName(String name) {
        return new UsernamePasswordAuthenticationToken(
            name,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
