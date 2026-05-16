package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LockerRepository extends JpaRepository<LockerEntity, Long> {

    @Query("""
        select l
        from LockerEntity l
        where l.id = :id
          and l.deleted = false
        """)
    Optional<LockerEntity> findActiveById(@Param("id") Long id);

    @Query(value = """
        -- 조회 기준 좌표
        WITH target AS (
            SELECT ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography AS point
        ),
        -- 반경 내 후보 추출하고 + 실제 거리 계산
        nearby AS (
            SELECT
                l.id,
                l.name,
                l.road_address,
                l.location,
                ST_Distance(l.location, target.point) AS distance_meters
            FROM lockers l
            CROSS JOIN target
            WHERE ST_DWithin(l.location, target.point, :radiusMeters)
              AND l.deleted = false
        )
        SELECT
            nearby.id AS id,
            nearby.name AS name,
            nearby.road_address AS roadAddress,
            ST_Y(nearby.location::geometry) AS latitude,
            ST_X(nearby.location::geometry) AS longitude,
            nearby.distance_meters AS distanceMeters,
            -- 20m 이내 후보를 같은 클러스터로 묶는다.
            ST_ClusterDBSCAN(
                ST_Transform(nearby.location::geometry, 3857),
                eps := 20,
                minpoints := 1
            ) OVER () AS clusterId
        FROM nearby
        ORDER BY distanceMeters ASC
        """, nativeQuery = true)
    List<NearbyLockerQueryProjection> findNearby(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("radiusMeters") int radiusMeters
    );
}
