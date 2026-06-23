package com.zimdugo.locker.entrypoint.dto.response.seo;

import com.zimdugo.locker.application.result.seo.LockerSeoResult;
import java.util.List;
import java.util.stream.Collectors;

public record LockerSeoListResponse(
    List<LockerSeoResponse> lockers
) {
    public static LockerSeoListResponse from(List<LockerSeoResult> results) {
        return new LockerSeoListResponse(
            results.stream()
                .map(LockerSeoResponse::from)
                .collect(Collectors.toList())
        );
    }
}
