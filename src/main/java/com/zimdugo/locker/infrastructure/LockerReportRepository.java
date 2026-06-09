package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LockerReportRepository extends JpaRepository<LockerReportEntity, Long> {

    @Query("SELECT COUNT(lr) FROM LockerReportEntity lr WHERE lr.user.id = :userId AND lr.deletedAt IS NULL")
    long countLockerReportsByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT lr
        FROM LockerReportEntity lr
        WHERE lr.id = :reportId
            AND lr.user.id = :userId
            AND lr.deletedAt IS NULL
        """)
    Optional<LockerReportEntity> findActiveByIdAndUserId(
        @Param("reportId") Long reportId,
        @Param("userId") Long userId
    );

    @Query(
        value = """
            WITH target AS (
                SELECT ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography AS point
            )
            SELECT
                lr.id AS reportId,
                lr.name AS lockerName,
                lr.road_address AS roadAddress,
                lr.locker_type AS lockerType,
                lr.latitude AS latitude,
                lr.longitude AS longitude,
                ST_Distance(
                    ST_SetSRID(ST_MakePoint(lr.longitude, lr.latitude), 4326)::geography,
                    target.point
                ) AS distanceMeters,
                lr.updated_at AS updatedAt
            FROM locker_reports lr
            CROSS JOIN target
            WHERE lr.user_id = :userId
                AND lr.deleted_at IS NULL
            ORDER BY lr.created_at DESC, lr.id DESC
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM locker_reports lr
            WHERE lr.user_id = :userId
                AND lr.deleted_at IS NULL
            """,
        nativeQuery = true
    )
    Page<MyLockerReportHistoryQueryProjection> findMyLockerReports(
        @Param("userId") Long userId,
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        Pageable pageable
    );
}
