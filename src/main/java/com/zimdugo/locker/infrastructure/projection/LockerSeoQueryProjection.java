package com.zimdugo.locker.infrastructure.projection;

public interface LockerSeoQueryProjection {
    Long getLockerId();
    String getLockerName();
    String getLanguageCode();
    String getTranslatedName();
}
