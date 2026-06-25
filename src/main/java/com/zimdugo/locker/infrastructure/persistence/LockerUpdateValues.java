package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.publication.PublicationStatus;

public record LockerUpdateValues(
    String name,
    String roadAddress,
    double latitude,
    double longitude,
    PlaceEntity place,
    PublicationStatus publicationStatus
) {
}
