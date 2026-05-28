package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.NearbyLocker;
import com.zimdugo.locker.domain.NearbyLockerReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerPinQueryService {

    private final NearbyLockerReader nearbyLockerReader;

    public List<LockerPinResponse> getPins(double latitude, double longitude, int radiusMeters) {
        List<NearbyLocker> nearbyLockers = nearbyLockerReader.findNearby(latitude, longitude, radiusMeters);
        if (nearbyLockers.isEmpty()) {
            return List.of();
        }

        Map<Integer, List<NearbyLocker>> grouped = groupByCluster(nearbyLockers);
        return toPins(grouped);
    }

    private Map<Integer, List<NearbyLocker>> groupByCluster(List<NearbyLocker> nearbyLockers) {
        Map<Integer, List<NearbyLocker>> grouped = new LinkedHashMap<>();
        for (NearbyLocker locker : nearbyLockers) {
            int key = locker.clusterId() == null ? locker.id().intValue() : locker.clusterId();
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(locker);
        }
        return grouped;
    }

    private List<LockerPinResponse> toPins(Map<Integer, List<NearbyLocker>> grouped) {
        List<LockerPinResponse> pins = new ArrayList<>(grouped.size());
        for (Map.Entry<Integer, List<NearbyLocker>> entry : grouped.entrySet()) {
            List<NearbyLocker> cluster = entry.getValue();
            if (cluster.size() == 1) {
                NearbyLocker locker = cluster.getFirst();
                pins.add(LockerPinResponse.locker(locker.id(), locker.latitude(), locker.longitude()));
                continue;
            }

            double latitudeSum = 0d;
            double longitudeSum = 0d;
            for (NearbyLocker locker : cluster) {
                latitudeSum += locker.latitude();
                longitudeSum += locker.longitude();
            }
            pins.add(
                LockerPinResponse.place(
                    entry.getKey().longValue(),
                    latitudeSum / cluster.size(),
                    longitudeSum / cluster.size()
                )
            );
        }
        return pins;
    }
}
