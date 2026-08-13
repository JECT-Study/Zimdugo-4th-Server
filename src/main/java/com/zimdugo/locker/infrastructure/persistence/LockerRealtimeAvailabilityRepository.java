package com.zimdugo.locker.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LockerRealtimeAvailabilityRepository extends JpaRepository<LockerRealtimeAvailabilityEntity, String> {
    List<LockerRealtimeAvailabilityEntity> findByExternalLockerIdIn(Collection<String> externalLockerIds);
}
