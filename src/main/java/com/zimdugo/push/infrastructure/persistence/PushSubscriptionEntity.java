package com.zimdugo.push.infrastructure.persistence;

import com.zimdugo.push.domain.PushLocale;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "push_subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushSubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, unique = true)
    private Long deviceId;

    @Column(nullable = false, length = 2048, unique = true)
    private String endpoint;

    @Column(nullable = false, length = 512)
    private String p256dh;

    @Column(nullable = false, length = 128)
    private String auth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PushLocale locale;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PushSubscriptionEntity(Long deviceId, String endpoint, String p256dh, String auth, PushLocale locale) {
        this.deviceId = deviceId;
        this.endpoint = endpoint;
        this.p256dh = p256dh;
        this.auth = auth;
        this.locale = locale;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public void update(String endpoint, String p256dh, String auth, PushLocale locale) {
        this.endpoint = endpoint;
        this.p256dh = p256dh;
        this.auth = auth;
        this.locale = locale;
        this.updatedAt = Instant.now();
    }

}
