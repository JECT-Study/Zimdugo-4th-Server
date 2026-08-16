package com.zimdugo.locker.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LockerImageRepository extends JpaRepository<LockerImageEntity, Long> {

    List<LockerImageEntity> findByLockerIdOrderByListOrderAsc(Long lockerId);

    void deleteByLockerId(Long lockerId);
}
