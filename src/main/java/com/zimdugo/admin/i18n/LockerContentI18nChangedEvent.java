package com.zimdugo.admin.i18n;

public record LockerContentI18nChangedEvent(Long placeId, Long lockerId) {

    public static LockerContentI18nChangedEvent place(Long placeId) {
        return new LockerContentI18nChangedEvent(placeId, null);
    }

    public static LockerContentI18nChangedEvent locker(Long lockerId) {
        return new LockerContentI18nChangedEvent(null, lockerId);
    }
}
