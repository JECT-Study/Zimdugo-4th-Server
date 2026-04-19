package com.zimdugo.user.infrastructure.persistence;

import com.zimdugo.identity.domain.AuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "social_account",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_provider_provider_user_id",
            columnNames = {"provider", "provider_user_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 100)
    private String providerUserId;

    @Column(length = 100)
    private String providerEmail;

    @Column(length = 255)
    private String providerProfileImageUrl;

    @Column(nullable = false)
    private LocalDateTime linkedAt;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public SocialAccountJpaEntity(
        Long id,
        UserEntity user,
        AuthProvider provider,
        String providerUserId,
        String providerEmail,
        String providerProfileImageUrl,
        LocalDateTime linkedAt
    ) {
        this.id = id;
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.providerEmail = providerEmail;
        this.providerProfileImageUrl = providerProfileImageUrl;
        this.linkedAt = linkedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.linkedAt == null) {
            this.linkedAt = LocalDateTime.now();
        }
    }
}
