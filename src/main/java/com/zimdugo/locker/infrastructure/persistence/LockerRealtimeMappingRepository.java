package com.zimdugo.locker.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LockerRealtimeMappingRepository extends JpaRepository<LockerRealtimeMappingEntity, String> {
    List<LockerRealtimeMappingEntity> findByExternalLockerIdIn(Collection<String> externalLockerIds);
}
