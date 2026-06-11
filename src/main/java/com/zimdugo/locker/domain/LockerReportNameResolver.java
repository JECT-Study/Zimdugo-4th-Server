package com.zimdugo.locker.domain;

public interface LockerReportNameResolver {
    String resolve(String roadAddress, String lockerType, double latitude, double longitude);
}
