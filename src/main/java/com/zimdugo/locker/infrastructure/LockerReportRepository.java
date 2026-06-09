package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LockerReportRepository extends JpaRepository<LockerReportEntity, Long> {

    @Query("SELECT COUNT(lr) FROM LockerReportEntity lr WHERE lr.user.id = :userId")
    long countLockerReportsByUserId(@Param("userId") Long userId);
}
