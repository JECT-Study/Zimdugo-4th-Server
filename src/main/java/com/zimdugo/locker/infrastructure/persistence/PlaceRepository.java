package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
import com.zimdugo.locker.infrastructure.projection.PlaceDetailQueryProjection;
import com.zimdugo.locker.infrastructure.projection.AdminPlaceCandidateProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



public interface PlaceRepository extends JpaRepository<PlaceEntity, Long> {

    @Query(value = """
        SELECT
            p.id AS placeId,
            COALESCE(pt.name, p.name) AS placeName,
            COALESCE(pt.road_address, p.road_address) AS roadAddress,
            p.latitude AS latitude,
            p.longitude AS longitude
        FROM places p
        LEFT JOIN place_translations pt ON pt.place_id = p.id AND pt.language_code = :languageCode
        WHERE p.id = :placeId
          AND p.publication_status = 'ACTIVE'
        """, nativeQuery = true)
    Optional<PlaceDetailQueryProjection> findPlaceDetailById(
        @Param("placeId") Long placeId,
        @Param("languageCode") String languageCode
    );

    List<PlaceEntity> findAllByRoadAddressOrderByNameAscIdAsc(String roadAddress);

    @Query(value = """
        WITH candidate_places AS (
            SELECT
                p.id AS placeId,
                p.name AS placeName,
                p.road_address AS roadAddress,
                p.latitude AS latitude,
                p.longitude AS longitude,
                ST_Distance(
                    ST_SetSRID(ST_MakePoint(p.longitude, p.latitude), 4326)::geography,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
                ) AS distanceMeters,
                LOWER(TRIM(p.road_address)) = LOWER(TRIM(:roadAddress)) AS exactAddress
            FROM places p
            WHERE p.publication_status = 'ACTIVE'
        )
        SELECT *
        FROM candidate_places
        WHERE exactAddress OR distanceMeters <= :radiusMeters
        ORDER BY exactAddress DESC, distanceMeters ASC, placeId ASC
        LIMIT 10
        """, nativeQuery = true)
    List<AdminPlaceCandidateProjection> findAdminCandidates(
        @Param("roadAddress") String roadAddress,
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("radiusMeters") int radiusMeters
    );
}
