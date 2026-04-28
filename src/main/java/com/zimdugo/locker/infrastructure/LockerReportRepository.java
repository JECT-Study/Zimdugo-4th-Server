package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.LockerReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LockerReportRepository extends JpaRepository<LockerReport, Long> {
}
