package com.zimdugo.locker.application.result.seo;

import com.zimdugo.locker.domain.seo.LockerSeo;
import java.util.Map;

public record LockerSeoResult(
    Long lockerId,
    Map<String, String> names
) {
    public static LockerSeoResult from(LockerSeo locker) {
        return new LockerSeoResult(
            locker.id(),
            locker.names()
        );
    }
}
