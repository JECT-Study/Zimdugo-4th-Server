package com.zimdugo.locker.domain;

import java.util.List;

public interface LockerSuggestCandidateReader {
    List<LockerSuggestCandidate> search(double latitude, double longitude, String keyword, int limit);
}
