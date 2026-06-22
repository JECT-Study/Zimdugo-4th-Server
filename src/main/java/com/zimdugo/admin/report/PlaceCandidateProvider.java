package com.zimdugo.admin.report;

import java.util.List;

public interface PlaceCandidateProvider {
    List<KakaoPlaceCandidate> findNearby(double latitude, double longitude);
}
