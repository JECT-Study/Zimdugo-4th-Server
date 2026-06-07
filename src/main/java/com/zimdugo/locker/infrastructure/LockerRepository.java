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
        )
        SELECT
            l.id AS lockerId,
            ST_Y(l.location::geometry) AS lockerLatitude,
            ST_X(l.location::geometry) AS lockerLongitude,
            l.place_id AS placeId
        FROM lockers l
        CROSS JOIN target
        WHERE ST_DWithin(l.location, target.point, :radiusMeters)
          AND l.place_id IS NOT NULL
        ORDER BY ST_Distance(l.location, target.point) ASC
        """, nativeQuery = true)
    List<NearbyLockerPlaceQueryProjection> findNearbyLockers(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("radiusMeters") int radiusMeters
    );

    @Query(value = """
        SELECT
            l.id AS lockerId,
            l.name AS lockerName,
            l.road_address AS roadAddress,
            ST_Y(l.location::geometry) AS lockerLatitude,
            ST_X(l.location::geometry) AS lockerLongitude,
            ld.locker_type AS lockerType,
            ld.indoor_outdoor_type AS indoorOutdoorType,
            ld.locker_size AS lockerSize,
            ld.min_price AS minPrice,
            ld.updated_at AS updatedAt,
            p.id AS placeId,
            p.name AS placeName
        FROM lockers l
        JOIN places p ON p.id = l.place_id
        JOIN locker_details ld ON ld.locker_id = l.id
        WHERE l.place_id IS NOT NULL
        """, nativeQuery = true)
    List<LockerSuggestIndexQueryProjection> findAllForSuggestIndex();

    @Query(value = """
        WITH target AS (
            SELECT ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography AS point
        )
        SELECT
            l.place_id AS placeId,
            l.id AS lockerId,
            l.name AS lockerName,
            l.road_address AS roadAddress,
            ld.locker_type AS lockerType,
            ld.indoor_outdoor_type AS indoorOutdoorType,
            ld.locker_size AS lockerSize,
            ld.min_price AS minPrice,
            ST_Y(l.location::geometry) AS lockerLatitude,
            ST_X(l.location::geometry) AS lockerLongitude,
            ST_Distance(l.location, target.point) AS distanceMeters,
            ld.updated_at AS updatedAt
        FROM lockers l
        JOIN locker_details ld ON ld.locker_id = l.id
        CROSS JOIN target
        WHERE l.place_id IN (:placeIds)
        ORDER BY l.place_id ASC, ST_Distance(l.location, target.point) ASC
        """, nativeQuery = true)
    List<LockerPlaceLockerQueryProjection> findByPlaceIds(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("placeIds") List<Long> placeIds
    );
}
