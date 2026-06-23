package com.zimdugo.locker.entrypoint.dto.response.seo;

import com.zimdugo.locker.application.result.seo.LockerSeoResult;
import java.util.Map;

public record LockerSeoResponse(
    Long lockerId,
    Map<String, String> names
) {
    public static LockerSeoResponse from(LockerSeoResult result) {
        return new LockerSeoResponse(
            result.lockerId(),
            result.names()
        );
    }
}
