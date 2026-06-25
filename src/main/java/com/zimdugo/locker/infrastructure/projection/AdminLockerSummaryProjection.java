package com.zimdugo.locker.infrastructure.projection;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.publication.PublicationStatus;

public interface AdminLockerSummaryProjection {

    Long getId();

    String getName();

    String getRoadAddress();

    PublicationStatus getPublicationStatus();

    LockerType getLockerType();

    IndoorOutdoorType getIndoorOutdoorType();

    Long getPlaceId();

    String getPlaceName();

    Integer getMinPrice();

    Integer getMaxPrice();
}
