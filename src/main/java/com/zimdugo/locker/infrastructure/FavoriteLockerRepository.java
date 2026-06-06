package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.FavoriteLockerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteLockerRepository extends JpaRepository<FavoriteLockerEntity, Long> {
    boolean existsByUserIdAndLockerId(Long userId, Long lockerId);

    @Modifying
    @Query(value = """
        INSERT INTO favorite_lockers (user_id, locker_id, created_at)
        VALUES (:userId, :lockerId, CURRENT_TIMESTAMP)
        ON CONFLICT (user_id, locker_id) DO NOTHING
        """, nativeQuery = true)
    int insertIgnoreConflict(@Param("userId") Long userId, @Param("lockerId") Long lockerId);

    long deleteByUserIdAndLockerId(Long userId, Long lockerId);
}
