package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.LockerReportStatus;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "locker_reports",
    indexes = {
        @Index(name = "idx_locker_reports_locker_id", columnList = "locker_id"),
        @Index(name = "idx_locker_reports_user_id", columnList = "user_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LockerReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locker_id")
    private LockerEntity locker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String roadAddress;

    @Column(length = 30)
    private String floor;

    @Column(length = 20)
    private String indoorOutdoorType;

    @Column(nullable = false, length = 20)
    private String lockerType;

    @Column(length = 100)
    private String sizeInfo;

    @Column(length = 100)
    private String priceInfo;

    @Column(length = 255)
    private String additionalInfo;

    private LocalTime startTime;

    private LocalTime endTime;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private boolean locationConsentAgreed;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LockerReportStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public LockerReportEntity(
        LockerEntity locker,
        UserEntity user,
        String name,
        String roadAddress,
        String floor,
        String indoorOutdoorType,
        String lockerType,
        String sizeInfo,
        String priceInfo,
        String additionalInfo,
        LocalTime startTime,
        LocalTime endTime,
        String imageUrl,
        boolean locationConsentAgreed,
        double latitude,
        double longitude
    ) {
        this.locker = locker;
        this.user = user;
        this.name = name;
        this.roadAddress = roadAddress;
        this.floor = floor;
        this.indoorOutdoorType = indoorOutdoorType;
        this.lockerType = lockerType != null ? lockerType : "UNKNOWN";
        this.sizeInfo = sizeInfo;
        this.priceInfo = priceInfo;
        this.additionalInfo = additionalInfo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.imageUrl = imageUrl;
        this.locationConsentAgreed = locationConsentAgreed;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = LockerReportStatus.SUBMITTED;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
