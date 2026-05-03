package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.NearbyLocker;
import com.zimdugo.locker.domain.NearbyLockerReader;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerReportDuplicateQueryService {

    private static final int MAX_CANDIDATE_COUNT = 5;

    private final NearbyLockerReader nearbyLockerReader;

    public LockerReportDuplicateResponse findDuplicates(
        double latitude,
        double longitude,
        int radiusMeters
    ) {
        var candidates = nearbyLockerReader.findNearby(latitude, longitude, radiusMeters)
            .stream()
            .sorted(Comparator.comparingDouble(NearbyLocker::distanceMeters))
            .limit(MAX_CANDIDATE_COUNT)
            .map(this::toResponse)
            .toList();

        return LockerReportDuplicateResponse.of(radiusMeters, candidates);
    }

    private LockerReportDuplicateCandidateResponse toResponse(NearbyLocker locker) {
        return new LockerReportDuplicateCandidateResponse(
            locker.id(),
            locker.name(),
            locker.roadAddress(),
            locker.latitude(),
            locker.longitude(),
            locker.distanceMeters()
        );
    }
}
