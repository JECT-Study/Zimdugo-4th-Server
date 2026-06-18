package com.zimdugo.locker.domain.locker;

public interface LockerStore {

    ReportLocker create(String name, String roadAddress, double latitude, double longitude);

    ReportLocker getById(Long id);
}
