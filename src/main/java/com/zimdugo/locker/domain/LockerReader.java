package com.zimdugo.locker.domain;

public interface LockerReader {
    boolean existsById(Long lockerId);
}
