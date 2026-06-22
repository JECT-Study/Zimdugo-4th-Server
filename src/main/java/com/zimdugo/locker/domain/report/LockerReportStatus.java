package com.zimdugo.locker.domain.report;

public enum LockerReportStatus {
    SUBMITTED("제보 접수"),
    TRANSLATION_REQUIRED("번역 필요"),
    READY_FOR_APPROVAL("승인 대기"),
    APPROVED("승인 완료"),
    REJECTED("반려");

    private final String displayName;

    LockerReportStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
