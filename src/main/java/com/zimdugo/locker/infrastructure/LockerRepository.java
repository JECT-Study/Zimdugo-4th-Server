package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LockerRepository extends JpaRepository<LockerEntity, Long> {

    @Query(value = """
        WITH target AS (
            SELECT ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography AS point
        ),
        nearby AS (
            SELECT
                l.id,
                l.name,
                l.road_address,
                l.location,
                l.place_id,
                ST_Distance(l.location, target.point) AS distance_meters
            FROM lockers l
            CROSS JOIN target
            WHERE ST_DWithin(l.location, target.point, :radiusMeters)
              AND l.place_id IS NOT NULL
        )
        SELECT
            nearby.id AS lockerId,
            nearby.name AS lockerName,
            nearby.road_address AS roadAddress,
            ST_Y(nearby.location::geometry) AS lockerLatitude,
            ST_X(nearby.location::geometry) AS lockerLongitude,
            nearby.distance_meters AS distanceMeters,
            COALESCE(latest_report.locker_type, 'UNKNOWN') AS lockerType,
            COALESCE(l.updated_at, latest_report.updated_at) AS updatedAt,
            FALSE AS isFavorite,
            p.id AS placeId,
            p.name AS placeName
        FROM nearby
        JOIN lockers l ON l.id = nearby.id
        JOIN places p ON p.id = nearby.place_id
        LEFT JOIN LATERAL (
            SELECT
                lr.locker_type,
                lr.updated_at
            FROM locker_reports lr
            WHERE lr.locker_id = nearby.id
            ORDER BY lr.updated_at DESC
            LIMIT 1
        ) latest_report ON TRUE
        ORDER BY distanceMeters ASC
        """, nativeQuery = true)
    List<NearbyLockerPlaceQueryProjection> findNearbyLockers(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("radiusMeters") int radiusMeters
    );
}
