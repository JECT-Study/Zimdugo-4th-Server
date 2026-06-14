package com.zimdugo.locker.domain;

import java.util.Optional;

public interface LockerDetailReader {
    Optional<LockerDetail> readById(Long lockerId);
}
