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
import com.zimdugo.locker.infrastructure.projection.AdminPlaceCandidateProjection;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public record AdminLockerReportReviewPageResult(
    Report report,
    List<ExistingPlace> existingPlaces,
    List<KakaoPlaceCandidate> kakaoPlaces,
    String kakaoError
) {
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
        String imageUrl
    ) {
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

        public static Report from(LockerReportEntity entity) {
            return new Report(
                entity.getId(), entity.getName(), entity.getRoadAddress(),
                entity.getGroundLevelType(), entity.getFloor(), entity.getIndoorOutdoorType(),
                entity.getLockerType(), entity.getLockerSize(), entity.getPriceType(),
                entity.getMinPrice(), entity.getMaxPrice(), entity.getOperatingTimeType(),
                entity.getStartTime(), entity.getEndTime(), entity.getAdditionalInfo(),
                entity.getLatitude(), entity.getLongitude(), entity.getStatus(),
                entity.getAppliedPlaceId(), entity.getAppliedLockerId(), entity.getReviewedBy(),
                entity.getRejectionMemo(), entity.getCreatedAt(),
                entity.getImage() == null ? null : entity.getImage().getImageUrl()
            );
        }
    }
}
