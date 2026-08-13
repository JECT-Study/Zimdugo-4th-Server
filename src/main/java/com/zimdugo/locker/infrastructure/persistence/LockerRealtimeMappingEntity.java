package com.zimdugo.locker.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locker_realtime_mappings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LockerRealtimeMappingEntity {

    @Id
    @Column(name = "external_locker_id", length = 100)
    private String externalLockerId;

    @Column(name = "locker_id", nullable = false)
    private Long lockerId;

    public LockerRealtimeMappingEntity(String externalLockerId, Long lockerId) {
        this.externalLockerId = externalLockerId;
        this.lockerId = lockerId;
    }
}
