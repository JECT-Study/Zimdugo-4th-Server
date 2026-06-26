package com.zimdugo.locker.infrastructure.persistence;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import static org.assertj.core.api.Assertions.assertThat;

class PublicationStatusRepositoryQueryTest {

    private static final String ACTIVE_LOCKER = "l.publication_status = 'ACTIVE'";
    private static final String ACTIVE_PLACE = "p.publication_status = 'ACTIVE'";

    @Test
    @DisplayName("모든 공개 보관함 조회는 활성 보관함만 반환한다")
    void lockerQueriesRequireActivePublicationStatus() {
        assertQueryContains(LockerRepository.class, "findDetailById", ACTIVE_LOCKER, ACTIVE_PLACE);
        assertQueryContains(LockerRepository.class, "findLockersWithinBounds", ACTIVE_LOCKER, ACTIVE_PLACE);
        assertQueryContains(LockerRepository.class, "findAllForSuggestIndex", ACTIVE_LOCKER, ACTIVE_PLACE);
        assertQueryContains(
            LockerRepository.class,
            "findAllForSuggestIndexByPlaceIds",
            ACTIVE_LOCKER,
            ACTIVE_PLACE
        );
        assertQueryContains(LockerRepository.class, "findPlaceIdsByLockerIds", "publicationStatus");
        assertQueryContains(LockerRepository.class, "findByPlaceIds", ACTIVE_LOCKER, ACTIVE_PLACE);
    }

    @Test
    @DisplayName("장소 상세와 관리자 기존 장소 후보는 활성 장소만 반환한다")
    void placeQueriesRequireActivePublicationStatus() {
        assertQueryContains(PlaceRepository.class, "findPlaceDetailById", ACTIVE_PLACE);
        assertQueryContains(PlaceRepository.class, "findAdminCandidates", ACTIVE_PLACE);
    }

    @Test
    @DisplayName("즐겨찾기 조회와 집계는 활성 보관함만 포함한다")
    void favoriteQueriesRequireActivePublicationStatus() {
        assertQueryContains(FavoriteLockerRepository.class, "countFavoriteLockersByUserId", ACTIVE_LOCKER);
        assertQueryContains(FavoriteLockerRepository.class, "findFavoriteLockerIds", "publicationStatus");
        assertQueryContains(FavoriteLockerRepository.class, "findFavoriteLockers", ACTIVE_LOCKER);
    }

    private void assertQueryContains(Class<?> repository, String methodName, String... fragments) {
        Method method = Arrays.stream(repository.getDeclaredMethods())
            .filter(candidate -> candidate.getName().equals(methodName))
            .findFirst()
            .orElseThrow();
        Query query = method.getAnnotation(Query.class);

        assertThat(query)
            .as("%s.%s must declare @Query", repository.getSimpleName(), methodName)
            .isNotNull();
        assertThat(query.value()).contains(fragments);
        if (!query.countQuery().isBlank()) {
            assertThat(query.countQuery()).contains(fragments);
        }
    }
}
