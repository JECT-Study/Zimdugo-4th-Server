package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.report.LockerReportStatus;
import com.zimdugo.locker.infrastructure.projection.AdminLockerReportListProjection;
import com.zimdugo.locker.infrastructure.projection.MyLockerReportHistoryQueryProjection;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LockerReportRepository extends JpaRepository<LockerReportEntity, Long> {

    long countByStatusIn(Collection<LockerReportStatus> statuses);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
        LocalDateTime start,
        LocalDateTime end
    );

    @Query("""
        SELECT
            lr.id AS id,
            lr.name AS name,
            lr.roadAddress AS roadAddress,
            lr.status AS status,
            lr.appliedAt AS appliedAt,
            lr.createdAt AS createdAt
        FROM LockerReportEntity lr
        ORDER BY lr.createdAt DESC, lr.id DESC
        """)
    List<AdminLockerReportListProjection> findRecentForAdminReportList(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT lr FROM LockerReportEntity lr WHERE lr.id = :reportId")
    Optional<LockerReportEntity> findByIdForUpdate(@Param("reportId") Long reportId);

    @Query("SELECT lr FROM LockerReportEntity lr WHERE lr.id = :reportId")
    Optional<LockerReportEntity> findActiveById(@Param("reportId") Long reportId);

    @Query("""
        SELECT lr
        FROM LockerReportEntity lr
        LEFT JOIN FETCH lr.image
        WHERE lr.id = :reportId
        """)
    Optional<LockerReportEntity> findActiveByIdWithImage(@Param("reportId") Long reportId);

    @Query(value = """
        SELECT ST_Distance(
            ST_SetSRID(ST_MakePoint(lr.longitude, lr.latitude), 4326)::geography,
            ST_SetSRID(ST_MakePoint(lri.gps_longitude, lri.gps_latitude), 4326)::geography
        )
        FROM locker_reports lr
        JOIN locker_report_images lri ON lri.report_id = lr.id
        WHERE lr.id = :reportId
            AND lri.gps_latitude IS NOT NULL
            AND lri.gps_longitude IS NOT NULL
        """, nativeQuery = true)
    Optional<Double> findImageDistanceMeters(@Param("reportId") Long reportId);

    @Query("SELECT COUNT(lr) FROM LockerReportEntity lr WHERE lr.user.id = :userId")
    long countLockerReportsByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT lr
        FROM LockerReportEntity lr
        LEFT JOIN FETCH lr.image
        WHERE lr.id = :reportId
            AND lr.user.id = :userId
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
                lri.image_url AS imageUrl,
                lr.latitude AS latitude,
                lr.longitude AS longitude,
                ST_Distance(
                    ST_SetSRID(ST_MakePoint(lr.longitude, lr.latitude), 4326)::geography,
                    target.point
                ) AS distanceMeters,
                lr.updated_at AS updatedAt
            FROM locker_reports lr
            LEFT JOIN locker_report_images lri ON lri.report_id = lr.id
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
