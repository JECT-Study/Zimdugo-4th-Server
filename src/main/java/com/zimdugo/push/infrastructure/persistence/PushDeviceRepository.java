package com.zimdugo.push.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushDeviceRepository extends JpaRepository<PushDeviceEntity, Long> {

    @Query("select device.id from PushDeviceEntity device where device.tokenHash = :tokenHash")
    Optional<Long> findIdByTokenHash(@Param("tokenHash") String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select device from PushDeviceEntity device where device.tokenHash = :tokenHash")
    Optional<PushDeviceEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);
}
