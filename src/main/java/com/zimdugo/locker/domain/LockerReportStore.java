package com.zimdugo.locker.domain;

public interface LockerReportStore {

    SavedLockerReport create(LockerReportCreateInfo createInfo);

    void update(Long userId, Long reportId, LockerReportUpdateInfo updateInfo);

    void delete(Long userId, Long reportId);
}
