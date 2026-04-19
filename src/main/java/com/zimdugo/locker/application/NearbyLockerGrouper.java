package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.NearbyLocker;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NearbyLockerGrouper {

    private static final double COORDINATE_ROUND_SCALE = 1_000_000d; // 위도/경도 소수점 6자리 = 약 0.1m 단위 라고 합니다.
    // 0.1m 단위는 오차가 좀 있어도 같은걸로 취급하도록 했어요

    public List<List<NearbyLocker>> groupByCoordinateOrRoadAddress(List<NearbyLocker> nearbyLockers) {
        int count = nearbyLockers.size();
        int[] parent = new int[count];
        for (int index = 0; index < count; index++) {
            parent[index] = index;
        }

        Map<NearbyLockerCoordinateKey, Integer> coordinateRepresentative = new LinkedHashMap<>();
        Map<String, Integer> roadAddressRepresentative = new LinkedHashMap<>();

        for (int index = 0; index < count; index++) {
            NearbyLocker locker = nearbyLockers.get(index);

            NearbyLockerCoordinateKey coordinateKey = coordinateKey(locker.latitude(), locker.longitude());
            Integer coordinateOwner = coordinateRepresentative.putIfAbsent(coordinateKey, index);
            if (coordinateOwner != null) {
                union(parent, coordinateOwner, index);
            }

            String roadAddressKey = roadAddressKey(locker.roadAddress());
            if (roadAddressKey != null) {
                Integer roadAddressOwner = roadAddressRepresentative.putIfAbsent(roadAddressKey, index);
                if (roadAddressOwner != null) {
                    union(parent, roadAddressOwner, index);
                }
            }
        }

        Map<Integer, List<NearbyLocker>> grouped = new LinkedHashMap<>();
        for (int index = 0; index < count; index++) {
            int root = find(parent, index);
            grouped.computeIfAbsent(root, ignored -> new ArrayList<>()).add(nearbyLockers.get(index));
        }

        return new ArrayList<>(grouped.values());
    }

    private NearbyLockerCoordinateKey coordinateKey(double latitude, double longitude) {
        long latitudeE6 = Math.round(latitude * COORDINATE_ROUND_SCALE);
        long longitudeE6 = Math.round(longitude * COORDINATE_ROUND_SCALE);
        return new NearbyLockerCoordinateKey(latitudeE6, longitudeE6);
    }

    private String roadAddressKey(String roadAddress) {
        if (roadAddress == null || roadAddress.isBlank()) {
            return null;
        }
        return roadAddress.trim().toLowerCase(Locale.ROOT);
    }

    private int find(int[] parent, int node) {
        if (parent[node] == node) {
            return node;
        }
        parent[node] = find(parent, parent[node]);
        return parent[node];
    }

    private void union(int[] parent, int first, int second) {
        int firstRoot = find(parent, first);
        int secondRoot = find(parent, second);
        if (firstRoot != secondRoot) {
            parent[secondRoot] = firstRoot;
        }
    }
}
