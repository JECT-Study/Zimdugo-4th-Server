package com.zimdugo.locker.domain;

import java.util.List;

public record LockerSearchCandidateResult(
    LockerSearchMatchType matchType,
    List<LockerSuggestCandidate> candidates
) {
    public static LockerSearchCandidateResult name(List<LockerSuggestCandidate> candidates) {
        return new LockerSearchCandidateResult(LockerSearchMatchType.NAME, candidates);
    }

    public static LockerSearchCandidateResult address(List<LockerSuggestCandidate> candidates) {
        return new LockerSearchCandidateResult(LockerSearchMatchType.ADDRESS, candidates);
    }

    public static LockerSearchCandidateResult empty() {
        return name(List.of());
    }
}
