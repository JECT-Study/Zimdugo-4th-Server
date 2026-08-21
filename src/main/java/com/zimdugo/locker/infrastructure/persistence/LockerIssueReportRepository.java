package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.issue.LockerIssueReportStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LockerIssueReportRepository extends JpaRepository<LockerIssueReportEntity, Long> {
    boolean existsByLockerId(Long lockerId);

    List<LockerIssueReportEntity> findAllByOrderByCreatedAtDesc();

    List<LockerIssueReportEntity> findAllByStatusOrderByCreatedAtDesc(LockerIssueReportStatus status);
}
