package com.zimdugo.locker.domain.locker;

public interface LockerReader {
    boolean existsById(Long lockerId);
}
