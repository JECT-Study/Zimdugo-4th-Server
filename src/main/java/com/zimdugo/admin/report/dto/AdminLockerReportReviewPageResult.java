package com.zimdugo.admin.report.dto;

import com.zimdugo.admin.report.KakaoPlaceCandidate;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.report.LockerReportOperatingTimeType;
import com.zimdugo.locker.domain.report.LockerReportPriceType;
import com.zimdugo.locker.domain.report.LockerReportStatus;
import com.zimdugo.locker.infrastructure.persistence.GroundLevelType;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReportImageEntity;
import com.zimdugo.locker.infrastructure.projection.AdminPlaceCandidateProjection;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import lombok.Builder;

public record AdminLockerReportReviewPageResult(
    Report report,
    List<ExistingPlace> existingPlaces,
    List<KakaoPlaceCandidate> kakaoPlaces,
    String kakaoError
) {
    public record MetadataEntry(
        String directory,
        String tagName,
        Integer tagType,
        String description,
        Object value
    ) {}

    public record ImageMetadata(
        String exifMetadataJson,
        Double gpsLatitude,
        Double gpsLongitude,
        Double gpsAltitude,
        LocalDateTime capturedAt,
        List<MetadataEntry> entries
    ) {
        public static ImageMetadata from(LockerReportImageEntity imageEntity, List<MetadataEntry> entries) {
            if (imageEntity == null) {
                return null;
            }
            return new ImageMetadata(
                imageEntity.getExifMetadataJson(),
                imageEntity.getGpsLatitude(),
                imageEntity.getGpsLongitude(),
                imageEntity.getGpsAltitude(),
                imageEntity.getCapturedAt(),
                entries
            );
        }
    }

    public record ExistingPlace(
        Long id,
        String name,
        String roadAddress,
        double latitude,
        double longitude,
        double distanceMeters,
        boolean exactAddress
    ) {
        public static ExistingPlace from(AdminPlaceCandidateProjection projection) {
            return new ExistingPlace(
                projection.getPlaceId(),
                projection.getPlaceName(),
                projection.getRoadAddress(),
                projection.getLatitude(),
                projection.getLongitude(),
                projection.getDistanceMeters(),
                Boolean.TRUE.equals(projection.getExactAddress())
            );
        }
    }

    @Builder
    public record Report(
        Long id,
        String name,
        String roadAddress,
        GroundLevelType groundLevelType,
        Integer floor,
        IndoorOutdoorType indoorOutdoorType,
        LockerType lockerType,
        Set<LockerSizeType> lockerSize,
        LockerReportPriceType priceType,
        Integer minPrice,
        Integer maxPrice,
        LockerReportOperatingTimeType operatingTimeType,
        LocalTime startTime,
        LocalTime endTime,
        String additionalInfo,
        double latitude,
        double longitude,
        LockerReportStatus status,
        Long appliedPlaceId,
        Long appliedLockerId,
        String reviewedBy,
        String rejectionMemo,
        LocalDateTime createdAt,
        String imageUrl,
        String appliedPlaceName,
        String appliedLockerName,
        ImageMetadata imageMetadata,
        Double imageDistanceMeters
    ) {
        private static final double VERY_HIGH_TRUST_DISTANCE_METERS = 50;
        private static final double HIGH_TRUST_DISTANCE_METERS = 100;
        private static final double NORMAL_TRUST_DISTANCE_METERS = 200;

        public String floorLabel() {
            if (groundLevelType == null || floor == null) {
                return "층 없음";
            }
            return switch (groundLevelType) {
                case ABOVE_GROUND -> "지상 " + floor + "층";
                case UNDERGROUND -> "지하 " + floor + "층";
            };
        }

        public String priceLabel() {
            if (priceType == null || priceType == LockerReportPriceType.UNKNOWN) {
                return "가격 정보 없음";
            }
            if (priceType == LockerReportPriceType.FREE) {
                return "무료";
            }
            if (minPrice == null || maxPrice == null) {
                return "가격 정보 없음";
            }
            return minPrice + " ~ " + maxPrice + "원";
        }

        public String operatingTimeLabel() {
            if (operatingTimeType == null || operatingTimeType == LockerReportOperatingTimeType.UNKNOWN) {
                return "운영 시간 정보 없음";
            }
            if (operatingTimeType == LockerReportOperatingTimeType.OPEN_24_HOURS) {
                return "24시간 운영";
            }
            if (startTime == null || endTime == null) {
                return "운영 시간 정보 없음";
            }
            return startTime + " ~ " + endTime;
        }

        public String checkLocationTrust() {
            if (imageDistanceMeters == null) {
                return "GPS 정보 없음 (신뢰도 판단 불가)";
            }
            String distanceStr = String.format("%.1f", imageDistanceMeters) + "m";
            if (imageDistanceMeters <= VERY_HIGH_TRUST_DISTANCE_METERS) {
                return "매우 높음 (거리 오차: " + distanceStr + ")";
            } else if (imageDistanceMeters <= HIGH_TRUST_DISTANCE_METERS) {
                return "높음 (거리 오차: " + distanceStr + ")";
            } else if (imageDistanceMeters <= NORMAL_TRUST_DISTANCE_METERS) {
                return "보통 (거리 오차: " + distanceStr + ")";
            } else {
                return "낮음 (거리 오차: " + distanceStr + ")";
            }
        }

        public String locationTrustClass() {
            if (imageDistanceMeters == null) {
                return "trust-unknown";
            }
            if (imageDistanceMeters <= VERY_HIGH_TRUST_DISTANCE_METERS) {
                return "trust-very-high";
            } else if (imageDistanceMeters <= HIGH_TRUST_DISTANCE_METERS) {
                return "trust-high";
            } else if (imageDistanceMeters <= NORMAL_TRUST_DISTANCE_METERS) {
                return "trust-normal";
            } else {
                return "trust-low";
            }
        }

        public static Report from(
            LockerReportEntity entity,
            String appliedPlaceName,
            String appliedLockerName,
            ImageMetadata imageMetadata,
            Double imageDistanceMeters
        ) {
            return builderFrom(entity)
                .appliedPlaceName(appliedPlaceName)
                .appliedLockerName(appliedLockerName)
                .imageMetadata(imageMetadata)
                .imageDistanceMeters(imageDistanceMeters)
                .build();
        }

        private static ReportBuilder builderFrom(LockerReportEntity entity) {
            return Report.builder()
                .id(entity.getId())
                .name(entity.getName())
                .roadAddress(entity.getRoadAddress())
                .groundLevelType(entity.getGroundLevelType())
                .floor(entity.getFloor())
                .indoorOutdoorType(entity.getIndoorOutdoorType())
                .lockerType(entity.getLockerType())
                .lockerSize(entity.getLockerSize())
                .priceType(entity.getPriceType())
                .minPrice(entity.getMinPrice())
                .maxPrice(entity.getMaxPrice())
                .operatingTimeType(entity.getOperatingTimeType())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .additionalInfo(entity.getAdditionalInfo())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .status(entity.getStatus())
                .appliedPlaceId(entity.getAppliedPlaceId())
                .appliedLockerId(entity.getAppliedLockerId())
                .reviewedBy(entity.getReviewedBy())
                .rejectionMemo(entity.getRejectionMemo())
                .createdAt(entity.getCreatedAt())
                .imageUrl(entity.getImage() == null ? null : entity.getImage().getImageUrl());
        }
    }
}
