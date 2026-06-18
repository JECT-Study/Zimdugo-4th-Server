package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.report.LockerReportCreateInfo;
import com.zimdugo.locker.domain.report.LockerReportOperatingTimeType;
import com.zimdugo.locker.domain.report.LockerReportPriceType;
import com.zimdugo.locker.domain.report.LockerReportStatus;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
@SQLDelete(sql = "UPDATE locker_reports SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
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

    @Column(length = 100)
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
    private Set<LockerSizeType> lockerSize = Set.of();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LockerReportPriceType priceType;

    @Column
    private Integer minPrice;

    @Column
    private Integer maxPrice;

    @Column(length = 255)
    private String additionalInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LockerReportOperatingTimeType operatingTimeType;

    private LocalTime startTime;

    private LocalTime endTime;

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

    @OneToOne(mappedBy = "report", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private LockerReportImageEntity image;

    public void addImage(LockerReportImageEntity image) {
        this.image = image;
    }

    public static LockerReportEntity of(LockerReportCreateInfo createInfo, UserEntity user) {
        return LockerReportEntity.builder()
            .user(user)
            .roadAddress(createInfo.roadAddress())
            .groundLevelType(toGroundLevelType(createInfo.groundLevelType()))
            .floor(createInfo.floorNumber())
            .indoorOutdoorType(toIndoorOutdoorType(createInfo.indoorOutdoorType()))
            .lockerType(toLockerType(createInfo.lockerType()))
            .lockerSize(toLockerSize(createInfo.sizeTypes()))
            .priceType(toPriceType(createInfo.priceType()))
            .minPrice(createInfo.minPrice())
            .maxPrice(createInfo.maxPrice())
            .additionalInfo(createInfo.additionalInfo())
            .operatingTimeType(toOperatingTimeType(createInfo.operatingTimeType()))
            .startTime(createInfo.startTime())
            .endTime(createInfo.endTime())
            .locationConsentAgreed(createInfo.locationConsentAgreed())
            .latitude(createInfo.latitude())
            .longitude(createInfo.longitude())
            .build();
    }



    private static GroundLevelType toGroundLevelType(String groundLevelType) {
        if (groundLevelType == null || groundLevelType.isBlank()) {
            return null;
        }
        try {
            return GroundLevelType.valueOf(groundLevelType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_LOCKER_REPORT_INPUT, e);
        }
    }

    private static IndoorOutdoorType toIndoorOutdoorType(String indoorOutdoorType) {
        try {
            return IndoorOutdoorType.valueOf(indoorOutdoorType);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.INVALID_LOCKER_REPORT_INPUT, e);
        }
    }

    private static LockerType toLockerType(String lockerType) {
        try {
            return LockerType.valueOf(lockerType);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.INVALID_LOCKER_REPORT_INPUT, e);
        }
    }

    private static LockerReportPriceType toPriceType(String priceType) {
        try {
            return LockerReportPriceType.valueOf(priceType);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.INVALID_LOCKER_REPORT_INPUT, e);
        }
    }

    private static LockerReportOperatingTimeType toOperatingTimeType(String operatingTimeType) {
        try {
            return LockerReportOperatingTimeType.valueOf(operatingTimeType);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.INVALID_LOCKER_REPORT_INPUT, e);
        }
    }

    private static Set<LockerSizeType> toLockerSize(List<String> sizeTypes) {
        if (sizeTypes == null || sizeTypes.isEmpty()) {
            return Set.of();
        }
        return sizeTypes.stream()
            .map(LockerSizeType::from)
            .collect(Collectors.toUnmodifiableSet());
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



    private void ensureReviewable() {
        if (status == LockerReportStatus.APPROVED || status == LockerReportStatus.REJECTED) {
            throw new BusinessException(ErrorCode.LOCKER_REPORT_ALREADY_REVIEWED);
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
