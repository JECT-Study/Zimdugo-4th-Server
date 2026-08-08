package com.zimdugo.locker.domain.search;

import java.util.List;

public record LockerSearchCandidateResult(
    LockerSearchMatchType matchType,
    List<LockerSearchCandidate> candidates
) {
    public static LockerSearchCandidateResult name(List<LockerSearchCandidate> candidates) {
        return new LockerSearchCandidateResult(LockerSearchMatchType.NAME, candidates);
    }

    public static LockerSearchCandidateResult address(List<LockerSearchCandidate> candidates) {
        return new LockerSearchCandidateResult(LockerSearchMatchType.ADDRESS, candidates);
    }

    public static LockerSearchCandidateResult empty() {
        return name(List.of());
    }
}
