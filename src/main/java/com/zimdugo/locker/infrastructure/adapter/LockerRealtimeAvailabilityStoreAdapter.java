package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.realtime.LockerRealtimeAvailabilitySnapshot;
import com.zimdugo.locker.domain.realtime.LockerRealtimeAvailabilityStore;
import com.zimdugo.locker.infrastructure.persistence.LockerRealtimeAvailabilityEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerRealtimeAvailabilityRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerRealtimeMappingRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LockerRealtimeAvailabilityStoreAdapter implements LockerRealtimeAvailabilityStore {

    private final LockerRealtimeMappingRepository mappingRepository;
    private final LockerRealtimeAvailabilityRepository availabilityRepository;

    @Override
    @Transactional
    public int saveMapped(
        Collection<LockerRealtimeAvailabilitySnapshot> snapshots,
        LocalDateTime fetchedAt
    ) {
        if (snapshots.isEmpty()) {
            return 0;
        }
        Set<String> externalLockerIds = validatedExternalLockerIds(snapshots);
        Set<String> mappedExternalLockerIds = mappingRepository.findByExternalLockerIdIn(externalLockerIds).stream()
            .map(mapping -> mapping.getExternalLockerId())
            .collect(Collectors.toUnmodifiableSet());
        if (mappedExternalLockerIds.isEmpty()) {
            return 0;
        }
        Map<String, LockerRealtimeAvailabilityEntity> existingByExternalLockerId = availabilityRepository
            .findByExternalLockerIdIn(mappedExternalLockerIds).stream()
            .collect(Collectors.toMap(LockerRealtimeAvailabilityEntity::getExternalLockerId, Function.identity()));
        List<LockerRealtimeAvailabilityEntity> availabilities = snapshots.stream()
            .filter(snapshot -> mappedExternalLockerIds.contains(snapshot.externalLockerId()))
            .map(snapshot -> availability(snapshot, existingByExternalLockerId, fetchedAt))
            .toList();
        List<LockerRealtimeAvailabilityEntity> newAvailabilities = availabilities.stream()
            .filter(LockerRealtimeAvailabilityEntity::isNew)
            .toList();
        if (!newAvailabilities.isEmpty()) {
            availabilityRepository.saveAll(newAvailabilities);
        }
        return availabilities.size();
    }

    private Set<String> validatedExternalLockerIds(
        Collection<LockerRealtimeAvailabilitySnapshot> snapshots
    ) {
        Set<String> externalLockerIds = snapshots.stream()
            .map(LockerRealtimeAvailabilitySnapshot::externalLockerId)
            .collect(Collectors.toUnmodifiableSet());
        if (externalLockerIds.size() != snapshots.size()) {
            throw new IllegalArgumentException("Duplicate external locker ID");
        }
        return externalLockerIds;
    }

    private LockerRealtimeAvailabilityEntity availability(
        LockerRealtimeAvailabilitySnapshot snapshot,
        Map<String, LockerRealtimeAvailabilityEntity> existingByExternalLockerId,
        LocalDateTime fetchedAt
    ) {
        LockerRealtimeAvailabilityEntity existing = existingByExternalLockerId.get(snapshot.externalLockerId());
        if (existing == null) {
            return new LockerRealtimeAvailabilityEntity(
                snapshot.externalLockerId(), snapshot.smallAvailableCount(), snapshot.mediumAvailableCount(),
                snapshot.largeAvailableCount(), fetchedAt
            );
        }
        existing.update(
            snapshot.smallAvailableCount(),
            snapshot.mediumAvailableCount(),
            snapshot.largeAvailableCount(),
            fetchedAt
        );
        return existing;
    }
}
