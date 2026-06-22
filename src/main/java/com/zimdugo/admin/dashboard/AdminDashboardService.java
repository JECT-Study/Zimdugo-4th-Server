package com.zimdugo.admin.dashboard;

import com.zimdugo.locker.domain.report.LockerReportStatus;
import com.zimdugo.locker.infrastructure.persistence.LockerReportRepository;
import com.zimdugo.user.domain.VisitorLogRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final LockerReportRepository lockerReportRepository;
    private final VisitorLogRepository visitorLogRepository;
    private final Clock clock;

    @Autowired
    public AdminDashboardService(
        LockerReportRepository lockerReportRepository,
        VisitorLogRepository visitorLogRepository
    ) {
        this(lockerReportRepository, visitorLogRepository, Clock.system(SEOUL_ZONE));
    }

    AdminDashboardService(
        LockerReportRepository lockerReportRepository,
        VisitorLogRepository visitorLogRepository,
        Clock clock
    ) {
        this.lockerReportRepository = lockerReportRepository;
        this.visitorLogRepository = visitorLogRepository;
        this.clock = clock;
    }

    public AdminDashboardResult getDashboard() {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime nextDayStart = today.plusDays(1).atStartOfDay();

        return new AdminDashboardResult(
            today,
            visitorLogRepository.countVisitorsByDate(today),
            lockerReportRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                dayStart,
                nextDayStart
            ),
            countPendingReports()
        );
    }

    private long countPendingReports() {
        return lockerReportRepository.countByStatusIn(EnumSet.of(
            LockerReportStatus.SUBMITTED,
            LockerReportStatus.TRANSLATION_REQUIRED,
            LockerReportStatus.READY_FOR_APPROVAL
        ));
    }
}
