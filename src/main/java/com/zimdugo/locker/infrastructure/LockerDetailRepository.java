package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.LockerDetailEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LockerDetailRepository extends JpaRepository<LockerDetailEntity, Long> {
    Optional<LockerDetailEntity> findByLockerId(Long lockerId);
}
