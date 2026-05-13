package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.UserLockerFavoriteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLockerFavoriteRepository extends JpaRepository<UserLockerFavoriteEntity, Long> {

    @EntityGraph(attributePaths = "locker")
    Page<UserLockerFavoriteEntity> findByUserIdAndLockerDeletedFalseOrderByDisplayOrderAscCreatedAtDesc(
        Long userId,
        Pageable pageable
    );

    boolean existsByUserIdAndLockerIdAndLockerDeletedFalse(Long userId, Long lockerId);

    void deleteByUserIdAndLockerId(Long userId, Long lockerId);

    java.util.List<UserLockerFavoriteEntity> findByUserIdAndLockerIdIn(
        Long userId,
        java.util.Collection<Long> lockerIds
    );

    java.util.List<UserLockerFavoriteEntity> findByUserIdAndLockerDeletedFalseAndLockerIdIn(
        Long userId,
        java.util.Collection<Long> lockerIds
    );

    long countByUserIdAndLockerDeletedFalse(Long userId);

    java.util.Optional<UserLockerFavoriteEntity> findTopByUserIdOrderByDisplayOrderDesc(Long userId);
}
