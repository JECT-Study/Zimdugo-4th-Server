package com.zimdugo.locker.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.zimdugo.locker.domain.realtime.LockerRealtimeAvailabilitySnapshot;
import com.zimdugo.locker.infrastructure.persistence.LockerRealtimeAvailabilityEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerRealtimeAvailabilityRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerRealtimeMappingEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerRealtimeMappingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LockerRealtimeAvailabilityStoreAdapterTest {

    @Mock
    private LockerRealtimeMappingRepository mappingRepository;

    @Mock
    private LockerRealtimeAvailabilityRepository availabilityRepository;

    @Captor
    private ArgumentCaptor<List<LockerRealtimeAvailabilityEntity>> availabilitiesCaptor;

    @InjectMocks
    private LockerRealtimeAvailabilityStoreAdapter adapter;

    @Test
    void savesOnlyMappedAvailabilityWithBulkReadsAndWrites() {
        LocalDateTime previousFetchedAt = LocalDateTime.of(2026, 8, 13, 9, 0);
        LocalDateTime fetchedAt = LocalDateTime.of(2026, 8, 13, 10, 0);
        LockerRealtimeAvailabilitySnapshot existingSnapshot = snapshot("TL1", 3, 2, 1);
        LockerRealtimeAvailabilitySnapshot newSnapshot = snapshot("TL2", 6, 5, 4);
        LockerRealtimeAvailabilitySnapshot unmappedSnapshot = snapshot("TL3", 9, 8, 7);
        LockerRealtimeAvailabilityEntity existing = new LockerRealtimeAvailabilityEntity(
            "TL1", 0, 0, 0, previousFetchedAt
        );
        LockerRealtimeAvailabilityEntity persistedExisting = org.mockito.Mockito.spy(existing);
        when(persistedExisting.isNew()).thenReturn(false);
        given(mappingRepository.findByExternalLockerIdIn(Set.of("TL1", "TL2", "TL3"))).willReturn(List.of(
            new LockerRealtimeMappingEntity("TL1", 1L),
            new LockerRealtimeMappingEntity("TL2", 2L)
        ));
        given(availabilityRepository.findByExternalLockerIdIn(Set.of("TL1", "TL2")))
            .willReturn(List.of(persistedExisting));

        int savedCount = adapter.saveMapped(
            List.of(existingSnapshot, newSnapshot, unmappedSnapshot),
            fetchedAt
        );

        verify(availabilityRepository).saveAll(availabilitiesCaptor.capture());
        assertThat(availabilitiesCaptor.getValue())
            .extracting(LockerRealtimeAvailabilityEntity::getExternalLockerId)
            .containsExactly("TL2");
        assertThat(persistedExisting.getSmallAvailableCount()).isEqualTo(3);
        assertThat(persistedExisting.getMediumAvailableCount()).isEqualTo(2);
        assertThat(persistedExisting.getLargeAvailableCount()).isEqualTo(1);
        assertThat(persistedExisting.getFetchedAt()).isEqualTo(fetchedAt);
        assertThat(savedCount).isEqualTo(2);
    }

    @Test
    void skipsAvailabilityPersistenceWhenNoSnapshotIsMapped() {
        LockerRealtimeAvailabilitySnapshot snapshot = snapshot("TL1", 3, 2, 1);
        given(mappingRepository.findByExternalLockerIdIn(Set.of("TL1"))).willReturn(List.of());

        int savedCount = adapter.saveMapped(
            List.of(snapshot),
            LocalDateTime.of(2026, 8, 13, 10, 0)
        );

        assertThat(savedCount).isZero();
        verifyNoInteractions(availabilityRepository);
    }

    @Test
    void rejectsDuplicateExternalLockerIdsBeforeAccessingPersistence() {
        LockerRealtimeAvailabilitySnapshot first = snapshot("TL1", 3, 2, 1);
        LockerRealtimeAvailabilitySnapshot duplicate = snapshot("TL1", 6, 5, 4);

        assertThatThrownBy(() -> adapter.saveMapped(
            List.of(first, duplicate),
            LocalDateTime.of(2026, 8, 13, 10, 0)
        )).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(mappingRepository, availabilityRepository);
    }

    private LockerRealtimeAvailabilitySnapshot snapshot(
        String externalLockerId,
        int smallAvailableCount,
        int mediumAvailableCount,
        int largeAvailableCount
    ) {
        return new LockerRealtimeAvailabilitySnapshot(
            externalLockerId,
            smallAvailableCount,
            mediumAvailableCount,
            largeAvailableCount
        );
    }
}
