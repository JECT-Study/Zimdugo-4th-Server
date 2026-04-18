package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.NearbyLockerReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerNearbyQueryService {

    private final NearbyLockerReader nearbyLockerReader;

    public List<NearbyLockerResponse> getNearbyLockers(double latitude, double longitude, int radiusMeters) {
        return nearbyLockerReader.findNearby(latitude, longitude, radiusMeters)
            .stream()
            .map(NearbyLockerResponse::from)
            .toList();
    }
}
