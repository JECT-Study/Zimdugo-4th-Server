package com.zimdugo.locker.domain;

import java.util.Optional;

public interface LockerPlaceReader {
    Optional<LockerPlace> readById(Long placeId);
}
