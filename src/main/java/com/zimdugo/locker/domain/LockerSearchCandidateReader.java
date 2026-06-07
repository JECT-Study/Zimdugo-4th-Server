package com.zimdugo.locker.domain;

public interface LockerSearchCandidateReader {
    LockerSearchCandidateResult search(
        double latitude,
        double longitude,
        String keyword,
        LockerSearchFilter filter
    );
}
