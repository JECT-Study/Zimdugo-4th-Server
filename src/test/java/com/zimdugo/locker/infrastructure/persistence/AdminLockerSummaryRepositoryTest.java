package com.zimdugo.locker.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.common.config.JpaAuditingConfig;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.infrastructure.projection.AdminLockerSummaryProjection;
import com.zimdugo.locker.domain.vote.LockerVoteType;
import com.zimdugo.user.domain.UserRole;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import com.zimdugo.user.infrastructure.persistence.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest(properties = "spring.test.database.replace=NONE")
@Import(JpaAuditingConfig.class)
class AdminLockerSummaryRepositoryTest {

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
    private LockerDetailRepository lockerDetailRepository;

    @Autowired
    private LockerAliasRepository lockerAliasRepository;

    @Autowired
    private LockerTranslationRepository lockerTranslationRepository;

    @Autowired
    private FavoriteLockerRepository favoriteLockerRepository;

    @Autowired
    private LockerVoteRepository lockerVoteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void adminSummaryQueriesSupportUnfilteredAndFilteredReads() {
        PlaceEntity place = placeRepository.save(
            new PlaceEntity("서울역", 37.55, 126.97, "서울 중구")
        );
        LockerEntity locker = lockerRepository.save(
            new LockerEntity("서울역 보관함", "서울 중구 세종대로", 37.55, 126.97, place)
        );
        lockerDetailRepository.save(new LockerDetailEntity(
            locker,
            new LockerDetailUpdateValues(
                LockerType.SUBWAY_STATION,
                IndoorOutdoorType.INDOOR,
                null,
                null,
                1000,
                3000,
                Set.of(),
                null,
                null,
                null,
                null
            )
        ));
        entityManager.flush();
        entityManager.clear();

        assertThat(lockerRepository.findAdminSummaries(PageRequest.of(0, 20)).getContent())
            .singleElement()
            .satisfies(summary -> assertThat(summary.getName()).isEqualTo("서울역 보관함"));
        assertThat(lockerRepository.searchAdminSummaries("서울역", PageRequest.of(0, 20)).getContent())
            .singleElement()
            .satisfies(summary -> assertThat(summary.getPlaceName()).isEqualTo("서울역"));
    }

    @Test
    void adminPlaceGroupPageFetchesAllLockersForSelectedPlaces() {
        PlaceEntity seoulStation = placeRepository.save(
            new PlaceEntity("서울역", 37.55, 126.97, "서울 중구")
        );
        PlaceEntity hongdae = placeRepository.save(
            new PlaceEntity("홍대입구", 37.56, 126.92, "서울 마포구")
        );
        LockerEntity oldSeoulLocker = locker("서울역 1층 물품보관함", seoulStation);
        locker("홍대입구 물품보관함", hongdae);
        LockerEntity latestSeoulLocker = locker("서울역 지상 2층", seoulStation);
        entityManager.flush();
        entityManager.clear();

        var firstPlacePage = lockerRepository.findAdminPlaceGroups(PageRequest.of(0, 1));

        assertThat(firstPlacePage.getTotalElements()).isEqualTo(2);
        assertThat(firstPlacePage.getContent())
            .singleElement()
            .satisfies(group -> {
                assertThat(group.getPlaceId()).isEqualTo(seoulStation.getId());
                assertThat(group.getPlaceName()).isEqualTo("서울역");
            });

        assertThat(lockerRepository.findAdminSummariesByPlaceIds(List.of(seoulStation.getId())))
            .extracting(AdminLockerSummaryProjection::getId)
            .containsExactly(latestSeoulLocker.getId(), oldSeoulLocker.getId());
    }

    @Test
    void dependentRowsCanBeDeletedBeforePermanentlyDeletingLocker() {
        LockerEntity locker = lockerRepository.save(
            new LockerEntity("삭제 대상", "서울 중구", 37.55, 126.97)
        );
        lockerDetailRepository.save(new LockerDetailEntity(
            locker,
            new LockerDetailUpdateValues(
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
            )
        ));
        lockerTranslationRepository.save(
            new LockerTranslationEntity(locker, SupportedLanguage.ENGLISH, "Delete", "Seoul")
        );
        lockerAliasRepository.save(
            new LockerAliasEntity(locker, SupportedLanguage.ENGLISH, "Delete locker")
        );
        UserEntity user = userRepository.save(new UserEntity(
            null,
            "delete-test@example.com",
            "삭제 테스트",
            null,
            UserStatus.ACTIVE,
            UserRole.USER,
            null,
            null
        ));
        favoriteLockerRepository.save(new FavoriteLockerEntity(user, locker));
        lockerVoteRepository.save(new LockerVoteEntity(user, locker, LockerVoteType.CORRECT));
        entityManager.flush();
        Long lockerId = locker.getId();
        entityManager.clear();
        LockerEntity storedLocker = lockerRepository.findById(lockerId).orElseThrow();

        lockerAliasRepository.deleteByLockerId(lockerId);
        lockerTranslationRepository.deleteByLockerId(lockerId);
        favoriteLockerRepository.deleteByLockerId(lockerId);
        lockerVoteRepository.deleteByLockerId(lockerId);
        lockerDetailRepository.deleteByLockerId(lockerId);
        lockerRepository.delete(storedLocker);
        entityManager.flush();

        assertThat(lockerRepository.findById(lockerId)).isEmpty();
    }

    private LockerEntity locker(String name, PlaceEntity place) {
        LockerEntity locker = lockerRepository.save(
            new LockerEntity(name, place.getRoadAddress(), place.getLatitude(), place.getLongitude(), place)
        );
        lockerDetailRepository.save(new LockerDetailEntity(
            locker,
            new LockerDetailUpdateValues(
                LockerType.SUBWAY_STATION,
                IndoorOutdoorType.INDOOR,
                null,
                null,
                1000,
                3000,
                Set.of(),
                null,
                null,
                null,
                null
            )
        ));
        return locker;
    }
}
