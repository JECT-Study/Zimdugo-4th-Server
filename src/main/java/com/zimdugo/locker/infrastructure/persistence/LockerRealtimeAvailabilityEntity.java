package com.zimdugo.locker.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "locker_realtime_availabilities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LockerRealtimeAvailabilityEntity implements Persistable<String> {

    @Id
    @Column(name = "external_locker_id", length = 100)
    private String externalLockerId;

    @Column(nullable = false)
    private int smallAvailableCount;

    @Column(nullable = false)
    private int mediumAvailableCount;

    @Column(nullable = false)
    private int largeAvailableCount;

    @Column(nullable = false)
    private LocalDateTime fetchedAt;

    @Transient
    private boolean newEntity;

    public LockerRealtimeAvailabilityEntity(
        String externalLockerId, int smallAvailableCount, int mediumAvailableCount,
        int largeAvailableCount, LocalDateTime fetchedAt
    ) {
        this.externalLockerId = externalLockerId;
        this.newEntity = true;
        update(smallAvailableCount, mediumAvailableCount, largeAvailableCount, fetchedAt);
    }

    @Override
    public String getId() {
        return externalLockerId;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    @PostLoad
    @PostPersist
    void markPersisted() {
        newEntity = false;
    }

    public void update(
        int smallAvailableCount,
        int mediumAvailableCount,
        int largeAvailableCount,
        LocalDateTime fetchedAt
    ) {
        this.smallAvailableCount = smallAvailableCount;
        this.mediumAvailableCount = mediumAvailableCount;
        this.largeAvailableCount = largeAvailableCount;
        this.fetchedAt = fetchedAt;
    }
}
