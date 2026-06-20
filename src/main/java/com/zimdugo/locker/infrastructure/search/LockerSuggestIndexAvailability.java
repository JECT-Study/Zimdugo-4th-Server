package com.zimdugo.locker.infrastructure.search;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("lockerSuggestIndex")
public class LockerSuggestIndexAvailability implements HealthIndicator {

    private final AtomicBoolean available = new AtomicBoolean();

    @Override
    public Health health() {
        return available.get() ? Health.up().build() : Health.down().build();
    }

    void markAvailable() {
        available.set(true);
    }
}
