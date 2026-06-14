package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
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
        """, nativeQuery = true)
    Optional<PlaceDetailQueryProjection> findPlaceDetailById(
        @Param("placeId") Long placeId,
        @Param("languageCode") String languageCode
    );
}
