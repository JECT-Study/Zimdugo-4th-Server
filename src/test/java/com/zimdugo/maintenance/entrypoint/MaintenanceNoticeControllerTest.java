package com.zimdugo.maintenance.entrypoint;

import com.zimdugo.maintenance.application.MaintenanceNoticeService;
import com.zimdugo.maintenance.application.dto.PublicMaintenanceNoticeResult;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MaintenanceNoticeControllerTest {

    @Mock
    private MaintenanceNoticeService maintenanceNoticeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MaintenanceNoticeController(maintenanceNoticeService))
            .build();
    }

    @Test
    @DisplayName("공개 점검 상태 API는 토글 기반 상태와 안내 정보를 반환한다")
    void returnsPublicMaintenanceStatus() throws Exception {
        given(maintenanceNoticeService.getPublicNotice()).willReturn(new PublicMaintenanceNoticeResult(
            true,
            "서비스 점검 중입니다",
            "더 나은 서비스 제공을 위해 점검을 진행하고 있습니다.",
            LocalDateTime.of(2026, 8, 30, 2, 0),
            null
        ));

        mockMvc.perform(get("/api/v1/maintenance-notice"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data.isUnderMaintenance").value(true))
            .andExpect(jsonPath("$.data.title").value("서비스 점검 중입니다"))
            .andExpect(jsonPath("$.data.endedAt").doesNotExist());
    }
}
