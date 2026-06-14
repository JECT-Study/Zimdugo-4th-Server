package com.zimdugo.locker.domain;

import java.util.Optional;

public interface LockerPlaceReader {
    Optional<LockerPlace> readById(Long placeId, String languageCode);

    default Optional<LockerPlace> readById(Long placeId) {
        return readById(placeId, "ko");
    }
}
