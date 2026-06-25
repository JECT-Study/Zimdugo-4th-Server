package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.infrastructure.persistence.LockerDetailEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



public interface LockerDetailRepository extends JpaRepository<LockerDetailEntity, Long> {
    Optional<LockerDetailEntity> findByLockerId(Long lockerId);

    @Modifying
    @Query("DELETE FROM LockerDetailEntity detail WHERE detail.locker.id = :lockerId")
    int deleteByLockerId(@Param("lockerId") Long lockerId);
}
