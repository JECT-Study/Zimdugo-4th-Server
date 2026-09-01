package com.zimdugo.push.entrypoint;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zimdugo.push.application.PushDeviceTokenHasher;
import com.zimdugo.push.application.PushReminderCreateService;
import com.zimdugo.push.application.PushReminderDeleteService;
import com.zimdugo.push.application.PushReminderQueryService;
import com.zimdugo.push.application.PushReminderResult;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PushReminderControllerTest {

    @Mock
    private PushReminderCreateService pushReminderCreateService;

    @Mock
    private PushReminderQueryService pushReminderQueryService;

    @Mock
    private PushReminderDeleteService pushReminderDeleteService;

    private MockMvc mockMvc;
    private PushDeviceTokenHasher pushDeviceTokenHasher;

    @BeforeEach
    void setUp() {
        pushDeviceTokenHasher = new PushDeviceTokenHasher();
        mockMvc = MockMvcBuilders.standaloneSetup(new PushReminderController(
            pushReminderCreateService,
            pushReminderQueryService,
            pushReminderDeleteService,
            pushDeviceTokenHasher
        )).build();
    }

    @Test
    void returnsTheCurrentDeviceActiveReminderWithUsageSnapshot() throws Exception {
        Instant startedAt = Instant.parse("2026-08-28T12:00:00Z");
        Instant endAt = Instant.parse("2026-08-28T12:10:00Z");
        given(pushReminderQueryService.getActive(pushDeviceTokenHasher.hash("device-token"))).willReturn(List.of(
            new PushReminderResult(456L, 123L, startedAt, endAt, 10, 8, 5)
        ));

        mockMvc.perform(get("/api/v1/push/reminders").cookie(new MockCookie("deviceToken", "device-token")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200"))
            .andExpect(jsonPath("$.data[0].id").value(456L))
            .andExpect(jsonPath("$.data[0].startedAt").value("2026-08-28T12:00:00Z"))
            .andExpect(jsonPath("$.data[0].endAt").value("2026-08-28T12:10:00Z"))
            .andExpect(jsonPath("$.data[0].totalUsageMinutes").value(10))
            .andExpect(jsonPath("$.data[0].remainingMinutes").value(8))
            .andExpect(jsonPath("$.data[0].remindBeforeMinutes").value(5));
    }

    @Test
    void deletesOnlyTheReminderOfTheCurrentDevice() throws Exception {
        mockMvc.perform(delete("/api/v1/push/reminders/456")
                .cookie(new MockCookie("deviceToken", "device-token")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("S200"))
            .andExpect(jsonPath("$.data").doesNotExist());

        verify(pushReminderDeleteService).delete(pushDeviceTokenHasher.hash("device-token"), 456L);
    }
}
