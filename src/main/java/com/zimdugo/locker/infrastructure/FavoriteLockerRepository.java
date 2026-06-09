package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.FavoriteLockerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteLockerRepository extends JpaRepository<FavoriteLockerEntity, Long> {
    boolean existsByUserIdAndLockerId(Long userId, Long lockerId);

    @Query("SELECT COUNT(fl) FROM FavoriteLockerEntity fl WHERE fl.user.id = :userId")
    long countFavoriteLockersByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = """
        INSERT INTO favorite_lockers (user_id, locker_id, created_at)
        VALUES (:userId, :lockerId, CURRENT_TIMESTAMP)
        ON CONFLICT (user_id, locker_id) DO NOTHING
        """, nativeQuery = true)
    int insertIgnoreConflict(@Param("userId") Long userId, @Param("lockerId") Long lockerId);

    long deleteByUserIdAndLockerId(Long userId, Long lockerId);

    @Query(
        value = """
            WITH target AS (
                SELECT ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography AS point
            )
            SELECT
                l.id AS lockerId,
                l.name AS lockerName,
                l.road_address AS roadAddress,
                ld.locker_type AS lockerType,
                l.latitude AS latitude,
                l.longitude AS longitude,
                ST_Distance(l.location, target.point) AS distanceMeters,
                ld.updated_at AS updatedAt
            FROM favorite_lockers fl
            JOIN lockers l ON l.id = fl.locker_id
            JOIN locker_details ld ON ld.locker_id = l.id
            CROSS JOIN target
            WHERE fl.user_id = :userId
            ORDER BY fl.created_at DESC, fl.id DESC
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM favorite_lockers fl
            JOIN lockers l ON l.id = fl.locker_id
            JOIN locker_details ld ON ld.locker_id = l.id
            WHERE fl.user_id = :userId
            """,
        nativeQuery = true
    )
    Page<FavoriteLockerListQueryProjection> findFavoriteLockers(
        @Param("userId") Long userId,
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        Pageable pageable
    );
}
