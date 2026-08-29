package com.zimdugo.push.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushDeviceRepository extends JpaRepository<PushDeviceEntity, Long> {

    Optional<Long> findIdByTokenHash(String tokenHash);
}
