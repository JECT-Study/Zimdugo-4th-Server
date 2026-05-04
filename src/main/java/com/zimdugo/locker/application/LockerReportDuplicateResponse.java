package com.zimdugo.locker.application;

import java.util.List;

public record LockerReportDuplicateResponse(
    boolean hasDuplicates,
    int radiusMeters,
    List<LockerReportDuplicateCandidateResponse> candidates
) {
    public static LockerReportDuplicateResponse of(
        int radiusMeters,
        List<LockerReportDuplicateCandidateResponse> candidates
    ) {
        return new LockerReportDuplicateResponse(!candidates.isEmpty(), radiusMeters, candidates);
    }
}
