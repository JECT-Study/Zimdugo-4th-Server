package com.zimdugo.admin.translation.dto;

import com.zimdugo.admin.i18n.dto.AdminLockerI18nResponse;
import com.zimdugo.admin.i18n.dto.AdminPlaceI18nResponse;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.report.LockerReportOperatingTimeType;
import com.zimdugo.locker.domain.report.LockerReportPriceType;
import com.zimdugo.locker.domain.report.LockerReportStatus;
import com.zimdugo.locker.infrastructure.persistence.GroundLevelType;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public record AdminLockerReportTranslationPageResult(
    Report report,
    AdminPlaceI18nResponse placeI18n,
    AdminLockerI18nResponse lockerI18n
) {
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
        String additionalInfo,
        LockerReportOperatingTimeType operatingTimeType,
        LocalTime startTime,
        LocalTime endTime,
        double latitude,
        double longitude,
        LockerReportStatus status,
        Long appliedPlaceId,
        Long appliedLockerId,
        LocalDateTime createdAt,
        String imageUrl
    ) {
        static Report from(LockerReportEntity entity) {
            return new Report(
                entity.getId(),
                entity.getName(),
                entity.getRoadAddress(),
                entity.getGroundLevelType(),
                entity.getFloor(),
                entity.getIndoorOutdoorType(),
                entity.getLockerType(),
                entity.getLockerSize(),
                entity.getPriceType(),
                entity.getMinPrice(),
                entity.getMaxPrice(),
                entity.getAdditionalInfo(),
                entity.getOperatingTimeType(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getStatus(),
                entity.getAppliedPlaceId(),
                entity.getAppliedLockerId(),
                entity.getCreatedAt(),
                entity.getImage() == null ? null : entity.getImage().getImageUrl()
            );
        }
    }

    public static AdminLockerReportTranslationPageResult of(
        LockerReportEntity report,
        AdminPlaceI18nResponse placeI18n,
        AdminLockerI18nResponse lockerI18n
    ) {
        return new AdminLockerReportTranslationPageResult(Report.from(report), placeI18n, lockerI18n);
    }
}
