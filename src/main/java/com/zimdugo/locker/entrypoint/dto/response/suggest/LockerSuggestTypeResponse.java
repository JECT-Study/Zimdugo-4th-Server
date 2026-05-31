package com.zimdugo.locker.entrypoint.dto.response.suggest;

import com.zimdugo.locker.application.result.suggest.LockerSuggestType;

public enum LockerSuggestTypeResponse {
    PLACE,
    LOCKER;

    public static LockerSuggestTypeResponse from(LockerSuggestType type) {
        return LockerSuggestTypeResponse.valueOf(type.name());
    }
}
