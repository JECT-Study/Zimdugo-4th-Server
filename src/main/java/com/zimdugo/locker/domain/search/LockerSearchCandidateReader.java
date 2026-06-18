package com.zimdugo.locker.domain.search;

public interface LockerSearchCandidateReader {
    LockerSearchCandidateResult search(
        double latitude,
        double longitude,
        String keyword,
        LockerSearchFilter filter
    );
}
