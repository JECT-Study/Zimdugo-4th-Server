package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.common.config.JpaAuditingConfig;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import jakarta.persistence.EntityManager;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = "spring.test.database.replace=NONE")
@Import(JpaAuditingConfig.class)
class PublicationStatusRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
        DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres")
    );

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private LockerDetailRepository lockerDetailRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void publicDetailQueriesReturnOnlyActivePlaceAndLocker() {
        PlaceEntity activePlace = placeRepository.save(
            new PlaceEntity("공개 장소", 37.55, 126.97, "서울 중구")
        );
        PlaceEntity draftPlace = placeRepository.save(
            PlaceEntity.draft("초안 장소", 37.56, 126.98, "서울 종로구")
        );
        LockerEntity activeLocker = saveLocker(
            new LockerEntity("공개 보관함", "서울 중구", 37.55, 126.97, activePlace)
        );
        LockerEntity draftLocker = saveLocker(
            LockerEntity.draft("초안 보관함", "서울 중구", 37.55, 126.97, activePlace)
        );
        LockerEntity lockerAtDraftPlace = saveLocker(
            new LockerEntity("초안 장소 보관함", "서울 종로구", 37.56, 126.98, draftPlace)
        );
        entityManager.flush();
        entityManager.clear();

        assertThat(lockerRepository.findDetailById(activeLocker.getId(), 999L, "ko"))
            .isPresent();
        assertThat(lockerRepository.findDetailById(draftLocker.getId(), 999L, "ko"))
            .isEmpty();
        assertThat(lockerRepository.findDetailById(lockerAtDraftPlace.getId(), 999L, "ko"))
            .isEmpty();
        assertThat(placeRepository.findPlaceDetailById(activePlace.getId(), "ko"))
            .isPresent();
        assertThat(placeRepository.findPlaceDetailById(draftPlace.getId(), "ko"))
            .isEmpty();
    }

    @Test
    void boundsQueryFiltersLockersBeforeReturningThem() {
        PlaceEntity place = placeRepository.save(new PlaceEntity("공개 장소", 37.55, 126.97, "서울 중구"));
        LockerEntity matchingLocker = saveLocker(
            new LockerEntity("대상", "서울 중구", 37.550, 126.970, place),
            LockerType.SUBWAY_STATION,
            IndoorOutdoorType.INDOOR,
            Set.of(LockerSizeType.LARGE)
        );
        saveLocker(
            new LockerEntity("크기 불일치", "서울 중구", 37.551, 126.971, place),
            LockerType.SUBWAY_STATION,
            IndoorOutdoorType.INDOOR,
            Set.of(LockerSizeType.MEDIUM)
        );
        saveLocker(
            new LockerEntity("실내외 불일치", "서울 중구", 37.552, 126.972, place),
            LockerType.SUBWAY_STATION,
            IndoorOutdoorType.OUTDOOR,
            Set.of(LockerSizeType.LARGE)
        );
        entityManager.flush();
        entityManager.createNativeQuery("""
            UPDATE lockers
            SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
            """).executeUpdate();
        entityManager.clear();

        var results = lockerRepository.findLockersWithinBounds(
            37.54,
            126.96,
            37.56,
            126.98,
            new LockerBoundsFilter(true, "LARGE", true, "INDOOR", true, "SUBWAY_STATION")
        );

        assertThat(results).extracting(projection -> projection.getLockerId()).containsExactly(matchingLocker.getId());
    }

    @Test
    void lockerDetailStoresEachLockerSizeInNormalizedTable() {
        LockerEntity locker = lockerRepository.save(new LockerEntity(
            "크기 정규화 대상",
            "서울 중구",
            37.55,
            126.97
        ));
        lockerDetailRepository.save(new LockerDetailEntity(
            locker,
            new LockerDetailUpdateValues(
                LockerType.ETC,
                IndoorOutdoorType.INDOOR,
                null,
                null,
                null,
                null,
                Set.of(LockerSizeType.SMALL, LockerSizeType.LARGE),
                null,
                null,
                null,
                null
            )
        ));
        entityManager.flush();

        @SuppressWarnings("unchecked")
        var sizeTypes = entityManager.createNativeQuery("""
            SELECT size_type
            FROM locker_size_types
            WHERE locker_id = :lockerId
            ORDER BY size_type
            """)
            .setParameter("lockerId", locker.getId())
            .getResultList();

        assertThat(sizeTypes).containsExactly("LARGE", "SMALL");
    }

    private LockerEntity saveLocker(LockerEntity locker) {
        return saveLocker(locker, LockerType.ETC, IndoorOutdoorType.INDOOR, Set.of());
    }

    private LockerEntity saveLocker(
        LockerEntity locker,
        LockerType lockerType,
        IndoorOutdoorType indoorOutdoorType,
        Set<LockerSizeType> lockerSize
    ) {
        LockerEntity savedLocker = lockerRepository.save(locker);
        lockerDetailRepository.save(new LockerDetailEntity(
            savedLocker,
            new LockerDetailUpdateValues(
                lockerType,
                indoorOutdoorType,
                null,
                null,
                null,
                null,
                lockerSize,
                null,
                null,
                null,
                null
            )
        ));
        return savedLocker;
    }
}
