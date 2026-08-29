package com.zimdugo.maintenance.infrastructure.persistence;

import com.zimdugo.maintenance.domain.MaintenanceNotice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "maintenance_notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaintenanceNoticeEntity {

    public static final long DEFAULT_ID = 1L;

    @Id
    private Long id;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime endedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private MaintenanceNoticeEntity(MaintenanceNotice notice) {
        this.id = DEFAULT_ID;
        update(notice);
    }

    public static MaintenanceNoticeEntity create(MaintenanceNotice notice) {
        return new MaintenanceNoticeEntity(notice);
    }

    public void update(MaintenanceNotice notice) {
        this.enabled = notice.enabled();
        this.title = notice.title();
        this.message = notice.message();
        this.startedAt = notice.startedAt();
        this.endedAt = notice.endedAt();
        this.updatedAt = LocalDateTime.now();
    }

    public MaintenanceNotice toDomain() {
        return MaintenanceNotice.of(enabled, title, message, startedAt, endedAt);
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
