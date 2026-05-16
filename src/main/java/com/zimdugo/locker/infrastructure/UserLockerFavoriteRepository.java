package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.UserLockerFavoriteEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    List<UserLockerFavoriteEntity> findByUserIdAndLockerIdIn(
        Long userId,
        Collection<Long> lockerIds
    );

    List<UserLockerFavoriteEntity> findByUserIdAndLockerDeletedFalseAndLockerIdIn(
        Long userId,
        Collection<Long> lockerIds
    );

    long countByUserIdAndLockerDeletedFalse(Long userId);

    Optional<UserLockerFavoriteEntity> findTopByUserIdAndLockerDeletedFalseOrderByDisplayOrderDesc(
        Long userId
    );
}
