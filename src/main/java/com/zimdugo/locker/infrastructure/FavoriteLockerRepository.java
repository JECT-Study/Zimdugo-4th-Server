package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.FavoriteLockerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteLockerRepository extends JpaRepository<FavoriteLockerEntity, Long> {
    @Query("""
        SELECT COUNT(fl) > 0
        FROM FavoriteLockerEntity fl
        WHERE fl.user.id = :userId
          AND fl.locker.id = :lockerId
        """)
    boolean existsByUserIdAndLockerId(@Param("userId") Long userId, @Param("lockerId") Long lockerId);

    @Modifying
    @Query("""
        DELETE
        FROM FavoriteLockerEntity fl
        WHERE fl.user.id = :userId
          AND fl.locker.id = :lockerId
        """)
    void deleteByUserIdAndLockerId(@Param("userId") Long userId, @Param("lockerId") Long lockerId);
}
