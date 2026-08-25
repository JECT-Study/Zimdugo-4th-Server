package com.zimdugo.locker.infrastructure.projection;

import java.time.LocalDateTime;

public interface LockerIssueReportLockerProjection {

    Long getId();

    String getName();

    String getRoadAddress();

    LocalDateTime getDeletedAt();

    default boolean isDeleted() {
        return getDeletedAt() != null;
    }
}
