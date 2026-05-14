package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.UserLockerFavoriteEntity;
import com.zimdugo.user.domain.UserRole;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserLockerFavoriteRepositoryTest {

    @Autowired
    private UserLockerFavoriteRepository userLockerFavoriteRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("사용자별 즐겨찾기 보관함을 표시 순서 기준으로 조회한다")
    void findByUserIdOrderByDisplayOrderAscCreatedAtDescReturnsOnlyUserFavorites() {
        UserEntity user = saveUser("favorite-user@example.com", "favorite-user");
        UserEntity otherUser = saveUser("other-favorite-user@example.com", "other-favorite-user");
        LockerEntity firstLocker = saveLocker("첫 번째 보관함");
        LockerEntity secondLocker = saveLocker("두 번째 보관함");
        LockerEntity otherLocker = saveLocker("다른 사용자의 보관함");

        saveFavorite(user, firstLocker, 1);
        saveFavorite(user, secondLocker, 0);
        saveFavorite(otherUser, otherLocker, 0);
        entityManager.flush();
        entityManager.clear();

        Page<UserLockerFavoriteEntity> result =
            userLockerFavoriteRepository.findByUserIdAndLockerDeletedFalseOrderByDisplayOrderAscCreatedAtDesc(
                user.getId(),
                PageRequest.of(0, 10)
            );

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
            .extracting(favorite -> favorite.getLocker().getName())
            .containsExactly("두 번째 보관함", "첫 번째 보관함");
    }

    @Test
    @DisplayName("삭제된 보관함은 즐겨찾기 목록 조회에서 제외한다")
    void findByUserIdExcludesDeletedLockers() {
        UserEntity user = saveUser("deleted-locker-user@example.com", "deleted-locker-user");
        LockerEntity activeLocker = saveLocker("정상 보관함");
        LockerEntity deletedLocker = saveLocker("삭제된 보관함");

        saveFavorite(user, activeLocker, 0);
        saveFavorite(user, deletedLocker, 1);
        deletedLocker.markDeleted();
        entityManager.flush();
        entityManager.clear();

        Page<UserLockerFavoriteEntity> result =
            userLockerFavoriteRepository.findByUserIdAndLockerDeletedFalseOrderByDisplayOrderAscCreatedAtDesc(
                user.getId(),
                PageRequest.of(0, 10)
            );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
            .extracting(favorite -> favorite.getLocker().getName())
            .containsExactly("정상 보관함");
    }

    @Test
    @DisplayName("사용자와 보관함 ID로 즐겨찾기를 삭제한다")
    void deleteByUserIdAndLockerIdDeletesFavorite() {
        UserEntity user = saveUser("delete-favorite-user@example.com", "delete-favorite-user");
        LockerEntity locker = saveLocker("삭제 대상 보관함");
        saveFavorite(user, locker, 0);
        entityManager.flush();

        userLockerFavoriteRepository.deleteByUserIdAndLockerId(user.getId(), locker.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(userLockerFavoriteRepository.existsByUserIdAndLockerIdAndLockerDeletedFalse(
            user.getId(),
            locker.getId()
        ))
            .isFalse();
    }

    @Test
    @DisplayName("주어진 보관함 ID 목록으로 사용자의 즐겨찾기를 조회한다")
    void findByUserIdAndLockerIdInReturnsMatchingFavorites() {
        UserEntity user = saveUser("match-user@example.com", "match-user");
        LockerEntity firstLocker = saveLocker("첫 번째 보관함");
        LockerEntity secondLocker = saveLocker("두 번째 보관함");
        LockerEntity thirdLocker = saveLocker("세 번째 보관함");

        saveFavorite(user, firstLocker, 0);
        saveFavorite(user, secondLocker, 1);
        saveFavorite(user, thirdLocker, 2);
        entityManager.flush();
        entityManager.clear();

        List<UserLockerFavoriteEntity> favorites = userLockerFavoriteRepository.findByUserIdAndLockerIdIn(
            user.getId(),
            List.of(firstLocker.getId(), thirdLocker.getId())
        );

        assertThat(favorites)
            .extracting(favorite -> favorite.getLocker().getId())
            .containsExactlyInAnyOrder(firstLocker.getId(), thirdLocker.getId());
    }

    @Test
    @DisplayName("삭제된 보관함은 reorder 대상 조회와 개수에서 제외한다")
    void activeFavoriteQueriesExcludeDeletedLockers() {
        UserEntity user = saveUser("reorder-user@example.com", "reorder-user");
        LockerEntity activeLocker = saveLocker("정상 보관함");
        LockerEntity deletedLocker = saveLocker("삭제된 보관함");

        saveFavorite(user, activeLocker, 0);
        saveFavorite(user, deletedLocker, 1);
        deletedLocker.markDeleted();
        entityManager.flush();
        entityManager.clear();

        List<UserLockerFavoriteEntity> favorites =
            userLockerFavoriteRepository.findByUserIdAndLockerDeletedFalseAndLockerIdIn(
                user.getId(),
                List.of(activeLocker.getId(), deletedLocker.getId())
            );

        assertThat(userLockerFavoriteRepository.countByUserIdAndLockerDeletedFalse(user.getId())).isEqualTo(1);
        assertThat(favorites)
            .extracting(favorite -> favorite.getLocker().getName())
            .containsExactly("정상 보관함");
    }

    @Test
    @DisplayName("삭제된 보관함은 최대 displayOrder 조회에서 제외한다")
    void findTopByUserIdAndLockerDeletedFalseOrderByDisplayOrderDescExcludesDeletedLocker() {
        UserEntity user = saveUser("display-order-user@example.com", "display-order-user");
        LockerEntity activeLocker = saveLocker("정상 보관함");
        LockerEntity deletedLocker = saveLocker("삭제된 보관함");

        saveFavorite(user, activeLocker, 1);
        saveFavorite(user, deletedLocker, 5);
        deletedLocker.markDeleted();
        entityManager.flush();
        entityManager.clear();

        UserLockerFavoriteEntity result =
            userLockerFavoriteRepository.findTopByUserIdAndLockerDeletedFalseOrderByDisplayOrderDesc(user.getId())
                .orElseThrow();

        assertThat(result.getLocker().getName()).isEqualTo("정상 보관함");
        assertThat(result.getDisplayOrder()).isEqualTo(1);
    }

    private UserLockerFavoriteEntity saveFavorite(UserEntity user, LockerEntity locker, int displayOrder) {
        UserLockerFavoriteEntity favorite = new UserLockerFavoriteEntity(user, locker, displayOrder);
        entityManager.persist(favorite);
        return favorite;
    }

    private UserEntity saveUser(String email, String nickname) {
        UserEntity user = new UserEntity(
            null,
            email,
            nickname,
            null,
            UserStatus.ACTIVE,
            UserRole.USER,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        entityManager.persist(user);
        return user;
    }

    private LockerEntity saveLocker(String name) {
        LockerEntity locker = new LockerEntity(
            name,
            "서울 마포구 양화로 160",
            37.556,
            126.923
        );
        entityManager.persist(locker);
        return locker;
    }
}
