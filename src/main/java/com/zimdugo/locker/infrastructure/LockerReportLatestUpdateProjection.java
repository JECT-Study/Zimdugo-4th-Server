package com.zimdugo.locker.infrastructure;

import java.time.LocalDateTime;

public interface LockerReportLatestUpdateProjection {

    Long getLockerId();

    LocalDateTime getLastCompletedVoteAt();
}
