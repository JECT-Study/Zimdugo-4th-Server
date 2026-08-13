package com.zimdugo.locker.application.realtime;

import com.zimdugo.locker.domain.realtime.LockerRealtimeAvailabilityProvider;
import com.zimdugo.locker.domain.realtime.LockerRealtimeAvailabilityStore;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LockerRealtimeAvailabilitySyncService {

    private final LockerRealtimeAvailabilityProvider provider;
    private final LockerRealtimeAvailabilityStore store;

    public int sync() {
        var available = provider.fetchAll();
        return store.saveMapped(available, LocalDateTime.now());
    }
}
