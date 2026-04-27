package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.DuplicateHandlingType;
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
public class LockerReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "locker_id", nullable = false)
    private LockerEntity locker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DuplicateHandlingType duplicateHandlingType;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String roadAddress;

    @Column(length = 255)
    private String detailLocation;

    @Column(length = 100)
    private String buildingName;

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

    @Column(length = 100)
    private String operatingHours;

    @Column(length = 500)
    private String imageUrl;

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
    public LockerReport(
        LockerEntity locker,
        UserEntity user,
        DuplicateHandlingType duplicateHandlingType,
        String name,
        String roadAddress,
        String detailLocation,
        String buildingName,
        String floor,
        String indoorOutdoorType,
        String lockerType,
        String sizeInfo,
        String priceInfo,
        String operatingHours,
        String imageUrl,
        double latitude,
        double longitude
    ) {
        this.locker = locker;
        this.user = user;
        this.duplicateHandlingType = duplicateHandlingType;
        this.name = name;
        this.roadAddress = roadAddress;
        this.detailLocation = detailLocation;
        this.buildingName = buildingName;
        this.floor = floor;
        this.indoorOutdoorType = indoorOutdoorType;
        this.lockerType = lockerType != null ? lockerType : "UNKNOWN";
        this.sizeInfo = sizeInfo;
        this.priceInfo = priceInfo;
        this.operatingHours = operatingHours;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = LockerReportStatus.COMPLETED;
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
