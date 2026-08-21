package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.publication.PublicationStatus;
import com.zimdugo.locker.infrastructure.projection.AdminLockerPlaceGroupProjection;
import com.zimdugo.locker.infrastructure.projection.AdminLockerSummaryProjection;
import com.zimdugo.locker.infrastructure.projection.LockerDetailQueryProjection;
import com.zimdugo.locker.infrastructure.projection.LockerPlaceLockerQueryProjection;
import com.zimdugo.locker.infrastructure.projection.LockerSeoQueryProjection;
import com.zimdugo.locker.infrastructure.projection.LockerSuggestIndexQueryProjection;
import com.zimdugo.locker.infrastructure.projection.LockerSizeTypeQueryProjection;
import com.zimdugo.locker.infrastructure.projection.NearbyLockerPlaceQueryProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface LockerRepository extends JpaRepository<LockerEntity, Long> {
    @Query(
        value = """
        SELECT
            p.id AS placeId,
            COALESCE(p.name, '장소 미지정') AS placeName
        FROM lockers l
        LEFT JOIN places p ON p.id = l.place_id
        WHERE l.deleted_at IS NULL
        GROUP BY p.id, p.name
        ORDER BY MAX(l.id) DESC
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM (
            SELECT COALESCE(l.place_id, 0)
            FROM lockers l
            WHERE l.deleted_at IS NULL
            GROUP BY COALESCE(l.place_id, 0)
        ) place_groups
        """,
        nativeQuery = true
    )
    Page<AdminLockerPlaceGroupProjection> findAdminPlaceGroups(Pageable pageable);
    @Query(
        value = """
        SELECT
            p.id AS placeId,
            COALESCE(p.name, '장소 미지정') AS placeName
        FROM lockers l
        LEFT JOIN places p ON p.id = l.place_id
        WHERE l.deleted_at IS NULL
          AND (
               LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(l.road_address) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        GROUP BY p.id, p.name
        ORDER BY MAX(l.id) DESC
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM (
            SELECT COALESCE(l.place_id, 0)
            FROM lockers l
            LEFT JOIN places p ON p.id = l.place_id
            WHERE l.deleted_at IS NULL
              AND (
               LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(l.road_address) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            GROUP BY COALESCE(l.place_id, 0)
        ) place_groups
        """,
        nativeQuery = true
    )
    Page<AdminLockerPlaceGroupProjection> searchAdminPlaceGroups(
        @Param("keyword") String keyword,
        Pageable pageable
    );
    @Query("""
        SELECT
            l.id AS id,
            l.name AS name,
            l.roadAddress AS roadAddress,
            l.publicationStatus AS publicationStatus,
            ld.lockerType AS lockerType,
            ld.indoorOutdoorType AS indoorOutdoorType,
            p.id AS placeId,
            p.name AS placeName,
            ld.minPrice AS minPrice,
            ld.maxPrice AS maxPrice
        FROM LockerEntity l
        LEFT JOIN LockerDetailEntity ld ON ld.locker = l
        LEFT JOIN l.place p
        ORDER BY l.id DESC
        """
    )
    Page<AdminLockerSummaryProjection> findAdminSummaries(Pageable pageable);
    @Query(
        value = """
        SELECT
            l.id AS id,
            l.name AS name,
            l.roadAddress AS roadAddress,
            l.publicationStatus AS publicationStatus,
            ld.lockerType AS lockerType,
            ld.indoorOutdoorType AS indoorOutdoorType,
            p.id AS placeId,
            p.name AS placeName,
            ld.minPrice AS minPrice,
            ld.maxPrice AS maxPrice
        FROM LockerEntity l
        LEFT JOIN LockerDetailEntity ld ON ld.locker = l
        LEFT JOIN l.place p
        WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(l.roadAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY l.id DESC
        """,
        countQuery = """
        SELECT COUNT(l.id)
        FROM LockerEntity l
        LEFT JOIN l.place p
        WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(l.roadAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """
    )
    Page<AdminLockerSummaryProjection> searchAdminSummaries(
        @Param("keyword") String keyword,
        Pageable pageable
    );
    @Query("""
        SELECT
            l.id AS id,
            l.name AS name,
            l.roadAddress AS roadAddress,
            l.publicationStatus AS publicationStatus,
            ld.lockerType AS lockerType,
            ld.indoorOutdoorType AS indoorOutdoorType,
            p.id AS placeId,
            p.name AS placeName,
            ld.minPrice AS minPrice,
            ld.maxPrice AS maxPrice
        FROM LockerEntity l
        LEFT JOIN LockerDetailEntity ld ON ld.locker = l
        LEFT JOIN l.place p
        WHERE p.id IN :placeIds
        ORDER BY l.id DESC
        """
    )
    List<AdminLockerSummaryProjection> findAdminSummariesByPlaceIds(@Param("placeIds") List<Long> placeIds);
    @Query("""
        SELECT
            l.id AS id,
            l.name AS name,
            l.roadAddress AS roadAddress,
            l.publicationStatus AS publicationStatus,
            ld.lockerType AS lockerType,
            ld.indoorOutdoorType AS indoorOutdoorType,
            p.id AS placeId,
            p.name AS placeName,
            ld.minPrice AS minPrice,
            ld.maxPrice AS maxPrice
        FROM LockerEntity l
        LEFT JOIN LockerDetailEntity ld ON ld.locker = l
        LEFT JOIN l.place p
        WHERE p.id IN :placeIds OR p.id IS NULL
        ORDER BY l.id DESC
        """
    )
    List<AdminLockerSummaryProjection> findAdminSummariesByPlaceIdsOrWithoutPlace(
        @Param("placeIds") List<Long> placeIds
    );
    @Query("""
        SELECT
            l.id AS id,
            l.name AS name,
            l.roadAddress AS roadAddress,
            l.publicationStatus AS publicationStatus,
            ld.lockerType AS lockerType,
            ld.indoorOutdoorType AS indoorOutdoorType,
            p.id AS placeId,
            p.name AS placeName,
            ld.minPrice AS minPrice,
            ld.maxPrice AS maxPrice
        FROM LockerEntity l
        LEFT JOIN LockerDetailEntity ld ON ld.locker = l
        LEFT JOIN l.place p
        WHERE p.id IS NULL
        ORDER BY l.id DESC
        """
    )
    List<AdminLockerSummaryProjection> findAdminSummariesWithoutPlace();
    @Query("""
        SELECT
            l.id AS id,
            l.name AS name,
            l.roadAddress AS roadAddress,
            l.publicationStatus AS publicationStatus,
            ld.lockerType AS lockerType,
            ld.indoorOutdoorType AS indoorOutdoorType,
            p.id AS placeId,
            p.name AS placeName,
            ld.minPrice AS minPrice,
            ld.maxPrice AS maxPrice
        FROM LockerEntity l
        LEFT JOIN LockerDetailEntity ld ON ld.locker = l
        LEFT JOIN l.place p
        WHERE p.id IN :placeIds
          AND (
              LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(l.roadAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY l.id DESC
        """
    )
    List<AdminLockerSummaryProjection> searchAdminSummariesByPlaceIds(
        @Param("placeIds") List<Long> placeIds,
        @Param("keyword") String keyword
    );
    @Query("""
        SELECT
            l.id AS id,
            l.name AS name,
            l.roadAddress AS roadAddress,
            l.publicationStatus AS publicationStatus,
            ld.lockerType AS lockerType,
            ld.indoorOutdoorType AS indoorOutdoorType,
            p.id AS placeId,
            p.name AS placeName,
            ld.minPrice AS minPrice,
            ld.maxPrice AS maxPrice
        FROM LockerEntity l
        LEFT JOIN LockerDetailEntity ld ON ld.locker = l
        LEFT JOIN l.place p
        WHERE (p.id IN :placeIds OR p.id IS NULL)
          AND (
              LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(l.roadAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY l.id DESC
        """
    )
    List<AdminLockerSummaryProjection> searchAdminSummariesByPlaceIdsOrWithoutPlace(
        @Param("placeIds") List<Long> placeIds,
        @Param("keyword") String keyword
    );

    @Query("""
        SELECT
            l.id AS id,
            l.name AS name,
            l.roadAddress AS roadAddress,
            l.publicationStatus AS publicationStatus,
            ld.lockerType AS lockerType,
            ld.indoorOutdoorType AS indoorOutdoorType,
            p.id AS placeId,
            p.name AS placeName,
            ld.minPrice AS minPrice,
            ld.maxPrice AS maxPrice
        FROM LockerEntity l
        LEFT JOIN LockerDetailEntity ld ON ld.locker = l
        LEFT JOIN l.place p
        WHERE p.id IS NULL
          AND (
              LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(l.roadAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY l.id DESC
        """
    )
    List<AdminLockerSummaryProjection> searchAdminSummariesWithoutPlace(@Param("keyword") String keyword);

    @Query(value = """
        SELECT
            l.id AS lockerId,
            COALESCE(lt.name, l.name) AS lockerName,
            COALESCE(lt.road_address, l.road_address) AS roadAddress,
            l.latitude AS latitude,
            l.longitude AS longitude,
            p.id AS placeId,
            COALESCE(pt.name, p.name) AS placeName,
            ld.locker_type AS lockerType,
            ld.indoor_outdoor_type AS indoorOutdoorType,
            ld.ground_level_type AS groundLevelType,
            ld.floor AS floor,
            ld.min_price AS minPrice,
            ld.max_price AS maxPrice,
            COALESCE(lt.detail_info, ld.detail_info) AS detailInfo,
            ld.start_time AS startTime,
            ld.end_time AS endTime,
            ld.image_url AS imageUrl,
            ld.accurate_vote_count AS accurateVoteCount,
            ld.inaccurate_vote_count AS inaccurateVoteCount,
            ld.created_at AS createdAt,
            ld.updated_at AS updatedAt,
            (CASE WHEN :userId IS NOT NULL AND EXISTS (
                SELECT 1 FROM favorite_lockers fl 
                WHERE fl.locker_id = l.id AND fl.user_id = :userId
            ) THEN true ELSE false END) AS isFavorite,
            (CASE WHEN :userId IS NOT NULL AND EXISTS (
                SELECT 1 FROM locker_votes lv 
                WHERE lv.locker_id = l.id AND lv.user_id = :userId AND lv.vote_type = 'CORRECT'
            ) THEN true ELSE false END) AS isAccurateVoted,
            (CASE WHEN :userId IS NOT NULL AND EXISTS (
                SELECT 1 FROM locker_votes lv 
                WHERE lv.locker_id = l.id AND lv.user_id = :userId AND lv.vote_type = 'INCORRECT'
            ) THEN true ELSE false END) AS isInaccurateVoted
            ,ra.small_available_count AS smallAvailableCount
            ,ra.medium_available_count AS mediumAvailableCount
            ,ra.large_available_count AS largeAvailableCount
            ,ra.fetched_at AS realtimeFetchedAt
        FROM lockers l
        JOIN locker_details ld ON ld.locker_id = l.id
        LEFT JOIN places p ON p.id = l.place_id
        LEFT JOIN locker_translations lt ON lt.locker_id = l.id AND lt.language_code = :languageCode
        LEFT JOIN place_translations pt ON pt.place_id = p.id AND pt.language_code = :languageCode
        LEFT JOIN LATERAL (
            SELECT SUM(ra.small_available_count) AS small_available_count,
                   SUM(ra.medium_available_count) AS medium_available_count,
                   SUM(ra.large_available_count) AS large_available_count,
                   MAX(ra.fetched_at) AS fetched_at
            FROM locker_realtime_mappings rm
            JOIN locker_realtime_availabilities ra ON ra.external_locker_id = rm.external_locker_id
            WHERE rm.locker_id = l.id
        ) ra ON true
        WHERE l.id = :lockerId
          AND l.deleted_at IS NULL
          AND l.publication_status = 'ACTIVE'
          AND p.publication_status = 'ACTIVE'
        """, nativeQuery = true)
    Optional<LockerDetailQueryProjection> findDetailById(
        @Param("lockerId") Long lockerId,
        @Param("userId") Long userId,
        @Param("languageCode") String languageCode
    );

    @Query(value = """
        SELECT
            l.id AS lockerId,
            ST_Y(l.location::geometry) AS lockerLatitude,
            ST_X(l.location::geometry) AS lockerLongitude,
            l.place_id AS placeId,
            ld.locker_type AS lockerType,
            ld.indoor_outdoor_type AS indoorOutdoorType
        FROM lockers l
        JOIN places p ON p.id = l.place_id
        JOIN locker_details ld ON ld.locker_id = l.id
        WHERE l.location && ST_MakeEnvelope(:swLng, :swLat, :neLng, :neLat, 4326)::geography
          AND l.deleted_at IS NULL
          AND l.place_id IS NOT NULL
          AND l.publication_status = 'ACTIVE'
          AND p.publication_status = 'ACTIVE'
          AND (
              :#{#filter.hasSizeTypes()} = false
              OR EXISTS (SELECT 1 FROM locker_size_types lst WHERE lst.locker_id = l.id
                         AND lst.size_type = ANY(string_to_array(:#{#filter.sizeTypes()}, ',')))
          )
          AND (
              :#{#filter.hasIndoorOutdoorTypes()} = false
              OR ld.indoor_outdoor_type = ANY(string_to_array(:#{#filter.indoorOutdoorTypes()}, ','))
          )
          AND (
              :#{#filter.hasLockerTypes()} = false
              OR ld.locker_type = ANY(string_to_array(:#{#filter.lockerTypes()}, ','))
          )
        ORDER BY l.id ASC
        """, nativeQuery = true)
    List<NearbyLockerPlaceQueryProjection> findLockersWithinBounds(
        @Param("swLat") double swLat,
        @Param("swLng") double swLng,
        @Param("neLat") double neLat,
        @Param("neLng") double neLng,
        @Param("filter") LockerBoundsFilter filter
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
            ld.min_price AS minPrice,
            ld.updated_at AS updatedAt,
            p.id AS placeId,
            p.name AS placeName,
            p.road_address AS placeRoadAddress
        FROM lockers l
        JOIN places p ON p.id = l.place_id
        JOIN locker_details ld ON ld.locker_id = l.id
        WHERE l.deleted_at IS NULL
          AND l.place_id IS NOT NULL
          AND l.publication_status = 'ACTIVE'
          AND p.publication_status = 'ACTIVE'
        """, nativeQuery = true)
    List<LockerSuggestIndexQueryProjection> findAllForSuggestIndex();

    @Query(value = """
        SELECT
            l.id AS lockerId,
            l.name AS lockerName,
            l.road_address AS roadAddress,
            ST_Y(l.location::geometry) AS lockerLatitude,
            ST_X(l.location::geometry) AS lockerLongitude,
            ld.locker_type AS lockerType,
            ld.indoor_outdoor_type AS indoorOutdoorType,
            ld.min_price AS minPrice,
            ld.updated_at AS updatedAt,
            p.id AS placeId,
            p.name AS placeName,
            p.road_address AS placeRoadAddress
        FROM lockers l
        JOIN places p ON p.id = l.place_id
        JOIN locker_details ld ON ld.locker_id = l.id
        WHERE l.deleted_at IS NULL
          AND l.place_id IN (:placeIds)
          AND l.publication_status = 'ACTIVE'
          AND p.publication_status = 'ACTIVE'
        """, nativeQuery = true)
    List<LockerSuggestIndexQueryProjection> findAllForSuggestIndexByPlaceIds(
        @Param("placeIds") List<Long> placeIds
    );

    @Query("""
        SELECT DISTINCT l.place.id
        FROM LockerEntity l
        WHERE l.id IN :lockerIds
          AND l.place IS NOT NULL
          AND l.publicationStatus = com.zimdugo.locker.domain.publication.PublicationStatus.ACTIVE
          AND l.place.publicationStatus = com.zimdugo.locker.domain.publication.PublicationStatus.ACTIVE
        """)
    List<Long> findPlaceIdsByLockerIds(@Param("lockerIds") List<Long> lockerIds);

    boolean existsByIdAndPublicationStatus(Long id, PublicationStatus publicationStatus);

    @Query(value = """
        WITH target AS (
            SELECT ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography AS point
        )
        SELECT
            l.place_id AS placeId,
            l.id AS lockerId,
            COALESCE(lt.name, l.name) AS lockerName,
            COALESCE(lt.road_address, l.road_address) AS roadAddress,
            ld.locker_type AS lockerType,
            ld.indoor_outdoor_type AS indoorOutdoorType,
            ld.min_price AS minPrice,
            ST_Y(l.location::geometry) AS lockerLatitude,
            ST_X(l.location::geometry) AS lockerLongitude,
            ST_Distance(l.location, target.point) AS distanceMeters,
            ld.updated_at AS updatedAt
        FROM lockers l
        JOIN locker_details ld ON ld.locker_id = l.id
        JOIN places p ON p.id = l.place_id
        LEFT JOIN locker_translations lt ON lt.locker_id = l.id AND lt.language_code = :languageCode
        CROSS JOIN target
        WHERE l.deleted_at IS NULL
          AND l.place_id IN (:placeIds)
          AND l.publication_status = 'ACTIVE'
          AND p.publication_status = 'ACTIVE'
        ORDER BY l.place_id ASC, ST_Distance(l.location, target.point) ASC
        """, nativeQuery = true)
    List<LockerPlaceLockerQueryProjection> findByPlaceIds(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("placeIds") List<Long> placeIds,
        @Param("languageCode") String languageCode
    );

    @Query(value = """
        SELECT locker_id AS lockerId, size_type AS sizeType
        FROM locker_size_types
        WHERE locker_id IN :lockerIds
        ORDER BY locker_id ASC, size_type ASC
        """, nativeQuery = true)
    List<LockerSizeTypeQueryProjection> findLockerSizeTypesByLockerIds(
        @Param("lockerIds") List<Long> lockerIds
    );

    @Query(value = """
        SELECT
            l.id AS lockerId,
            l.name AS lockerName,
            lt.language_code AS languageCode,
            lt.name AS translatedName
        FROM lockers l
        JOIN locker_details ld ON ld.locker_id = l.id
        LEFT JOIN locker_translations lt ON lt.locker_id = l.id
        WHERE l.deleted_at IS NULL
          AND l.publication_status = 'ACTIVE'
        ORDER BY l.id DESC
        """, nativeQuery = true)
    List<LockerSeoQueryProjection> findAllForSeo();
}
