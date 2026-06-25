package com.zimdugo.admin.locker.dto;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.publication.PublicationStatus;
import com.zimdugo.locker.infrastructure.persistence.GroundLevelType;
import com.zimdugo.locker.infrastructure.persistence.LockerDetailEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

public record AdminLockerDetailResult(
    Long id,
    String name,
    String roadAddress,
    double latitude,
    double longitude,
    Long placeId,
    String placeName,
    PublicationStatus publicationStatus,
    LockerType lockerType,
    IndoorOutdoorType indoorOutdoorType,
    GroundLevelType groundLevelType,
    Integer floor,
    Integer minPrice,
    Integer maxPrice,
    Set<LockerSizeType> lockerSizes,
    String detailInfo,
    LocalTime startTime,
    LocalTime endTime,
    String imageUrl,
    int accurateVoteCount,
    int inaccurateVoteCount
) {

    public static AdminLockerDetailResult from(LockerEntity locker, LockerDetailEntity detail) {
        return new AdminLockerDetailResult(
            locker.getId(),
            locker.getName(),
            locker.getRoadAddress(),
            locker.getLatitude(),
            locker.getLongitude(),
            locker.getPlace() == null ? null : locker.getPlace().getId(),
            locker.getPlace() == null ? null : locker.getPlace().getName(),
            locker.getPublicationStatus(),
            detail.getLockerType(),
            detail.getIndoorOutdoorType(),
            detail.getGroundLevelType(),
            detail.getFloor(),
            detail.getMinPrice(),
            detail.getMaxPrice(),
            detail.getLockerSize(),
            detail.getDetailInfo(),
            detail.getStartTime(),
            detail.getEndTime(),
            detail.getImageUrl(),
            detail.getAccurateVoteCount(),
            detail.getInaccurateVoteCount()
        );
    }

    public String lockerTypeLabel() {
        return AdminLockerDisplay.lockerType(lockerType);
    }

    public String indoorOutdoorLabel() {
        return AdminLockerDisplay.indoorOutdoor(indoorOutdoorType);
    }

    public String priceLabel() {
        return AdminLockerDisplay.price(minPrice, maxPrice);
    }

    public String lockerSizeLabel() {
        if (lockerSizes == null || lockerSizes.isEmpty()) {
            return "-";
        }
        return lockerSizes.stream()
            .sorted()
            .map(AdminLockerDisplay::lockerSize)
            .collect(Collectors.joining(", "));
    }

    public String floorLabel() {
        if (groundLevelType == null || floor == null) {
            return "-";
        }
        return switch (groundLevelType) {
            case ABOVE_GROUND -> "지상 " + floor + "층";
            case UNDERGROUND -> "지하 " + floor + "층";
        };
    }

    public boolean active() {
        return publicationStatus == PublicationStatus.ACTIVE;
    }
}
