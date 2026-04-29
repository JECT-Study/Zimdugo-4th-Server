package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LockerReportRepository extends JpaRepository<LockerReportEntity, Long> {
}
