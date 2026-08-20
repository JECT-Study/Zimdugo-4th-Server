package com.zimdugo.locker.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LockerIssueReportRepository extends JpaRepository<LockerIssueReportEntity, Long> {
}
