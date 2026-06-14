package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.IndoorOutdoorType;
import com.zimdugo.locker.domain.LockerReportStatus;
import com.zimdugo.locker.domain.LockerSizeType;
import com.zimdugo.locker.domain.LockerType;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "locker_reports",
    indexes = {
        @Index(name = "idx_locker_reports_user_id", columnList = "user_id")
    }
)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LockerReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String roadAddress;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GroundLevelType groundLevelType;

    @Column
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private IndoorOutdoorType indoorOutdoorType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LockerType lockerType;

    @Convert(converter = LockerSizeTypeConverter.class)
    @Column(length = 100)
    @Builder.Default
    private java.util.Set<LockerSizeType> lockerSize = java.util.Set.of();

    @Column
    private Boolean isFree;

    @Column
    private Integer minPrice;

    @Column
    private Integer maxPrice;

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
    @Builder.Default
    private LockerReportStatus status = LockerReportStatus.SUBMITTED;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    @Column
    private Long appliedPlaceId;

    @Column
    private Long appliedLockerId;

    @Column(length = 100)
    private String reviewedBy;

    @Column(length = 1000)
    private String reviewNote;

    @Column
    private LocalDateTime reviewedAt;

    public void updateReport(UpdateValues values) {
        ensureUserEditable();
        this.name = values.name();
        this.roadAddress = values.roadAddress();
        this.groundLevelType = values.groundLevelType();
        this.floor = values.floor();
        this.indoorOutdoorType = values.indoorOutdoorType();
        this.lockerType = values.lockerType();
        this.lockerSize = values.lockerSize();
        this.isFree = values.isFree();
        this.minPrice = values.minPrice();
        this.maxPrice = values.maxPrice();
        this.additionalInfo = values.additionalInfo();
        this.startTime = values.startTime();
        this.endTime = values.endTime();
        this.imageUrl = values.imageUrl();
        this.locationConsentAgreed = values.locationConsentAgreed();
        this.latitude = values.latitude();
        this.longitude = values.longitude();
    }

    public record UpdateValues(
        String name,
        String roadAddress,
        GroundLevelType groundLevelType,
        Integer floor,
        IndoorOutdoorType indoorOutdoorType,
        LockerType lockerType,
        Set<LockerSizeType> lockerSize,
        Boolean isFree,
        Integer minPrice,
        Integer maxPrice,
        String additionalInfo,
        LocalTime startTime,
        LocalTime endTime,
        String imageUrl,
        boolean locationConsentAgreed,
        double latitude,
        double longitude
    ) {
        @SuppressWarnings("checkstyle:ParameterNumber")
        public UpdateValues(
            String name,
            String roadAddress,
            GroundLevelType groundLevelType,
            Integer floor,
            IndoorOutdoorType indoorOutdoorType,
            LockerType lockerType,
            Set<LockerSizeType> lockerSize,
            Boolean isFree,
            Integer minPrice,
            Integer maxPrice,
            String additionalInfo,
            LocalTime startTime,
            LocalTime endTime,
            boolean locationConsentAgreed,
            double latitude,
            double longitude
        ) {
            this(
                name,
                roadAddress,
                groundLevelType,
                floor,
                indoorOutdoorType,
                lockerType,
                lockerSize,
                isFree,
                minPrice,
                maxPrice,
                additionalInfo,
                startTime,
                endTime,
                null,
                locationConsentAgreed,
                latitude,
                longitude
            );
        }
    }

    public void delete() {
        ensureUserEditable();
        this.deletedAt = LocalDateTime.now();
    }

    public void approve(Long placeId, Long lockerId, String reviewer, String reviewNote) {
        ensureReviewable();
        this.status = LockerReportStatus.APPROVED;
        this.appliedPlaceId = placeId;
        this.appliedLockerId = lockerId;
        recordReview(reviewer, reviewNote);
    }

    public void reject(String reviewer, String reviewNote) {
        ensureReviewable();
        this.status = LockerReportStatus.REJECTED;
        recordReview(reviewer, reviewNote);
    }

    private void ensureUserEditable() {
        if (status == LockerReportStatus.APPROVED) {
            throw new com.zimdugo.core.exception.BusinessException(
                com.zimdugo.core.exception.ErrorCode.LOCKER_REPORT_APPROVED_NOT_EDITABLE
            );
        }
    }

    private void ensureReviewable() {
        if (status == LockerReportStatus.APPROVED || status == LockerReportStatus.REJECTED) {
            throw new com.zimdugo.core.exception.BusinessException(
                com.zimdugo.core.exception.ErrorCode.LOCKER_REPORT_ALREADY_REVIEWED
            );
        }
    }

    private void recordReview(String reviewer, String reviewNote) {
        this.reviewedBy = reviewer;
        this.reviewNote = reviewNote;
        this.reviewedAt = LocalDateTime.now();
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
