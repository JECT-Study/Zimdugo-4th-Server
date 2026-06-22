package com.zimdugo.admin.dashboard;

import com.zimdugo.locker.domain.report.LockerReportStatus;
import com.zimdugo.locker.infrastructure.persistence.LockerReportRepository;
import com.zimdugo.user.domain.VisitorLogRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminDashboardServiceTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final Clock FIXED_CLOCK = Clock.fixed(
        Instant.parse("2026-06-22T03:00:00Z"),
        SEOUL_ZONE
    );

    private LockerReportRepository lockerReportRepository;
    private VisitorLogRepository visitorLogRepository;
    private AdminDashboardService dashboardService;

    @BeforeEach
    void setUp() {
        lockerReportRepository = mock(LockerReportRepository.class);
        visitorLogRepository = mock(VisitorLogRepository.class);
        dashboardService = new AdminDashboardService(
            lockerReportRepository,
            visitorLogRepository,
            FIXED_CLOCK
        );
    }

    @Test
    void summarizesTodayVisitorsReportsAndPendingReports() {
        LocalDateTime dayStart = LocalDateTime.of(2026, 6, 22, 0, 0);
        LocalDateTime nextDayStart = LocalDateTime.of(2026, 6, 23, 0, 0);
        when(visitorLogRepository.countVisitorsBetween(
            dayStart,
            nextDayStart
        )).thenReturn(17L);
        when(lockerReportRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            dayStart,
            nextDayStart
        )).thenReturn(4L);
        when(lockerReportRepository.countByStatusIn(EnumSet.of(
            LockerReportStatus.SUBMITTED,
            LockerReportStatus.TRANSLATION_REQUIRED,
            LockerReportStatus.READY_FOR_APPROVAL
        ))).thenReturn(10L);

        AdminDashboardResult result = dashboardService.getDashboard();

        assertThat(result.date()).isEqualTo(LocalDate.of(2026, 6, 22));
        assertThat(result.todayVisitors()).isEqualTo(17L);
        assertThat(result.todayReports()).isEqualTo(4L);
        assertThat(result.pendingReports()).isEqualTo(10L);
        verify(visitorLogRepository).countVisitorsBetween(
            dayStart,
            nextDayStart
        );
        verify(lockerReportRepository).countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            dayStart,
            nextDayStart
        );
        verify(lockerReportRepository).countByStatusIn(EnumSet.of(
            LockerReportStatus.SUBMITTED,
            LockerReportStatus.TRANSLATION_REQUIRED,
            LockerReportStatus.READY_FOR_APPROVAL
        ));
    }
}
