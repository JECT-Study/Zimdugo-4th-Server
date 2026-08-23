package com.zimdugo.locker.domain.detail;

import java.util.Optional;

public interface LockerDetailReader {
    Optional<LockerDetail> readById(
        Long lockerId,
        Long userId,
        String languageCode,
        double latitude,
        double longitude
    );

    default Optional<LockerDetail> readById(Long lockerId) {
        return readById(lockerId, null, "ko", 0, 0);
    }
}
