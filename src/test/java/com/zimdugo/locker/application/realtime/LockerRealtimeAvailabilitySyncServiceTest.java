package com.zimdugo.locker.application.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.zimdugo.locker.domain.realtime.LockerRealtimeAvailabilityProvider;
import com.zimdugo.locker.domain.realtime.LockerRealtimeAvailabilitySnapshot;
import com.zimdugo.locker.domain.realtime.LockerRealtimeAvailabilityStore;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LockerRealtimeAvailabilitySyncServiceTest {

    @Mock
    private LockerRealtimeAvailabilityProvider provider;

    @Mock
    private LockerRealtimeAvailabilityStore store;

    @InjectMocks
    private LockerRealtimeAvailabilitySyncService service;

    @Test
    void delegatesFetchedAvailabilityToTheStoreInOneBatch() {
        LockerRealtimeAvailabilitySnapshot mapped = new LockerRealtimeAvailabilitySnapshot("TL1", 3, 2, 1);
        LockerRealtimeAvailabilitySnapshot unmapped = new LockerRealtimeAvailabilitySnapshot("TL2", 2, 1, 0);
        given(provider.fetchAll()).willReturn(List.of(mapped, unmapped));
        given(store.saveMapped(eq(List.of(mapped, unmapped)), any(LocalDateTime.class))).willReturn(1);

        int syncedCount = service.sync();

        ArgumentCaptor<LocalDateTime> fetchedAt = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(store).saveMapped(eq(List.of(mapped, unmapped)), fetchedAt.capture());
        assertThat(fetchedAt.getValue()).isNotNull();
        assertThat(syncedCount).isEqualTo(1);
    }
}
