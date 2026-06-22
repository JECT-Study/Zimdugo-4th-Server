package com.zimdugo.user.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaVisitorLogRepository extends JpaRepository<VisitorLogEntity, Long> {

    @Modifying
    @Query(value = "INSERT INTO visitor_logs (visitor_identifier, accessed_date, accessed_at, user_id) " +
                   "VALUES (:identifier, :date, :accessedAt, :userId) " +
                   "ON CONFLICT (visitor_identifier, accessed_date) DO NOTHING", nativeQuery = true)
    void saveVisitorLog(
        @Param("identifier") String identifier,
        @Param("date") LocalDate date,
        @Param("accessedAt") LocalDateTime accessedAt,
        @Param("userId") Long userId
    );

    @Query("SELECT COUNT(v) FROM VisitorLogEntity v WHERE v.accessedAt >= :start AND v.accessedAt < :end")
    long countVisitorsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
