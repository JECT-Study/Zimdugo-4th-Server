package com.zimdugo.admin.translation.dto;

import com.zimdugo.admin.i18n.dto.AdminLockerI18nResponse;
import com.zimdugo.admin.i18n.dto.AdminPlaceI18nResponse;
import com.zimdugo.admin.locker.dto.AdminLockerDisplay;
import com.zimdugo.common.i18n.SupportedLanguage;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record AdminLockerReportTranslationPageResult(
    Report report,
    String appliedPlaceName,
    String appliedLockerName,
    AdminPlaceI18nResponse placeI18n,
    AdminLockerI18nResponse lockerI18n,
    boolean placeTranslationComplete,
    boolean lockerTranslationComplete
) {
    public boolean translationComplete() {
        return placeTranslationComplete && lockerTranslationComplete;
    }

    public List<LanguageReview> languages() {
        return SupportedLanguage.translationTargets().stream()
            .map(this::languageReview)
            .toList();
    }

    private LanguageReview languageReview(SupportedLanguage language) {
        AdminPlaceI18nResponse.Translation placeTranslation = placeI18n.translations().stream()
            .filter(item -> item.language() == language)
            .findFirst()
            .orElse(null);
        AdminLockerI18nResponse.Translation lockerTranslation = lockerI18n.translations().stream()
            .filter(item -> item.language() == language)
            .findFirst()
            .orElse(null);
        return new LanguageReview(
            language,
            new PlaceReview(
                placeTranslation == null ? "" : placeTranslation.name(),
                placeTranslation == null ? "" : placeTranslation.roadAddress(),
                placeAliases(language)
            ),
            new LockerReview(
                lockerTranslation == null ? "" : lockerTranslation.name(),
                lockerTranslation == null ? "" : lockerTranslation.roadAddress(),
                lockerTranslation == null ? "" : lockerTranslation.detailInfo(),
                lockerAliases(language)
            )
        );
    }

    private String placeAliases(SupportedLanguage language) {
        return placeI18n.aliases().stream()
            .filter(alias -> alias.language() == language)
            .map(AdminPlaceI18nResponse.Alias::alias)
            .collect(java.util.stream.Collectors.joining("\n"));
    }

    private String lockerAliases(SupportedLanguage language) {
        return lockerI18n.aliases().stream()
            .filter(alias -> alias.language() == language)
            .map(AdminLockerI18nResponse.Alias::alias)
            .collect(java.util.stream.Collectors.joining("\n"));
    }

    public record LanguageReview(
        SupportedLanguage language,
        PlaceReview place,
        LockerReview locker
    ) {
    }

    public record PlaceReview(String name, String roadAddress, String aliases) {
    }

    public record LockerReview(
        String name,
        String roadAddress,
        String detailInfo,
        String aliases
    ) {
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
        public String floorLabel() {
            if (groundLevelType == null || floor == null) {
                return "층 없음";
            }
            return switch (groundLevelType) {
                case ABOVE_GROUND -> "지상 " + floor + "층";
                case UNDERGROUND -> "지하 " + floor + "층";
            };
        }

        public String environmentTypeLabel() {
            return AdminLockerDisplay.indoorOutdoor(indoorOutdoorType) + " / "
                + AdminLockerDisplay.lockerType(lockerType);
        }

        public String lockerSizeLabel() {
            if (lockerSize == null || lockerSize.isEmpty()) {
                return "-";
            }
            return lockerSize.stream()
                .sorted()
                .map(AdminLockerDisplay::lockerSize)
                .collect(Collectors.joining(", "));
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

    @SuppressWarnings("checkstyle:ParameterNumber")
    public static AdminLockerReportTranslationPageResult of(
        LockerReportEntity report,
        String appliedPlaceName,
        String appliedLockerName,
        AdminPlaceI18nResponse placeI18n,
        AdminLockerI18nResponse lockerI18n,
        boolean placeTranslationComplete,
        boolean lockerTranslationComplete
    ) {
        return new AdminLockerReportTranslationPageResult(
            Report.from(report),
            appliedPlaceName,
            appliedLockerName,
            placeI18n,
            lockerI18n,
            placeTranslationComplete,
            lockerTranslationComplete
        );
    }
}
