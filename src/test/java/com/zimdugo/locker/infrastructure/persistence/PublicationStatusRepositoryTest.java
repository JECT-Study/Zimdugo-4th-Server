package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.common.config.JpaAuditingConfig;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
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

    private LockerEntity saveLocker(LockerEntity locker) {
        LockerEntity savedLocker = lockerRepository.save(locker);
        lockerDetailRepository.save(new LockerDetailEntity(
            savedLocker,
            LockerType.ETC,
            IndoorOutdoorType.INDOOR,
            null,
            null,
            null,
            null,
            Set.of(),
            null,
            null,
            null,
            null
        ));
        return savedLocker;
    }
}
