package com.zimdugo.admin.locker.dto;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.publication.PublicationStatus;
import com.zimdugo.locker.infrastructure.projection.AdminLockerSummaryProjection;

public record AdminLockerSummaryResult(
    Long id,
    String name,
    String roadAddress,
    PublicationStatus publicationStatus,
    LockerType lockerType,
    IndoorOutdoorType indoorOutdoorType,
    Long placeId,
    String placeName,
    Integer minPrice,
    Integer maxPrice
) {

    public static AdminLockerSummaryResult from(AdminLockerSummaryProjection projection) {
        return new AdminLockerSummaryResult(
            projection.getId(),
            projection.getName(),
            projection.getRoadAddress(),
            projection.getPublicationStatus(),
            projection.getLockerType(),
            projection.getIndoorOutdoorType(),
            projection.getPlaceId(),
            projection.getPlaceName(),
            projection.getMinPrice(),
            projection.getMaxPrice()
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

    public boolean active() {
        return publicationStatus == PublicationStatus.ACTIVE;
    }
}
