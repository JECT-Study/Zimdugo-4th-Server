package com.zimdugo.locker.infrastructure.projection;

import com.zimdugo.locker.domain.report.LockerReportStatus;
import java.time.LocalDateTime;

public interface AdminLockerReportListProjection {

    Long getId();

    String getName();

    String getRoadAddress();

    LockerReportStatus getStatus();

    LocalDateTime getAppliedAt();

    LocalDateTime getCreatedAt();
}
