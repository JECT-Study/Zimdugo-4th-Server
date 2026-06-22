package com.zimdugo.user.application;

import com.zimdugo.user.domain.VisitorAccessEvent;
import com.zimdugo.user.domain.VisitorLogRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VisitorLogServiceTest {

    @Test
    void handleVisitorAccessEventDelegatesToRepository() {
        VisitorLogRepository visitorLogRepository = mock(VisitorLogRepository.class);
        VisitorLogService service = new VisitorLogService(visitorLogRepository);

        LocalDate date = LocalDate.of(2026, 6, 22);
        LocalDateTime now = LocalDateTime.of(2026, 6, 22, 12, 34, 56);
        VisitorAccessEvent event = new VisitorAccessEvent("visitor-123", 42L, date, now);

        service.handleVisitorAccessEvent(event);

        verify(visitorLogRepository).saveAccessLog("visitor-123", 42L, date, now);
    }

    @Test
    void handleVisitorAccessEventHandlesExceptionSilently() {
        VisitorLogRepository visitorLogRepository = mock(VisitorLogRepository.class);
        doThrow(new RuntimeException("Database error"))
            .when(visitorLogRepository).saveAccessLog(any(), any(), any(), any());

        VisitorLogService service = new VisitorLogService(visitorLogRepository);

        LocalDate date = LocalDate.of(2026, 6, 22);
        LocalDateTime now = LocalDateTime.of(2026, 6, 22, 12, 34, 56);
        VisitorAccessEvent event = new VisitorAccessEvent("visitor-123", 42L, date, now);

        assertDoesNotThrow(() -> service.handleVisitorAccessEvent(event));
    }
}
