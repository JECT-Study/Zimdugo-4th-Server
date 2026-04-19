package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.Locker;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LockerRepository extends JpaRepository<Locker, Long> {

    @Query(value = """
        WITH target AS (
            SELECT ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography AS point
        )
        SELECT
            l.id AS id,
            l.name AS name,
            l.road_address AS roadAddress,
            ST_Y(l.location::geometry) AS latitude,
            ST_X(l.location::geometry) AS longitude,
            ST_Distance(l.location, target.point) AS distanceMeters
        FROM lockers l
        CROSS JOIN target
        WHERE ST_DWithin(l.location, target.point, :radiusMeters)
        ORDER BY distanceMeters ASC
        """, nativeQuery = true)
    List<NearbyLockerQueryProjection> findNearby(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("radiusMeters") int radiusMeters
    );
}
