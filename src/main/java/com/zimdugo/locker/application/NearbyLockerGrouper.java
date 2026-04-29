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

    private static final double COORDINATE_ROUND_SCALE = 1_000_000d;

    public List<List<NearbyLocker>> groupByCoordinateOrRoadAddress(List<NearbyLocker> nearbyLockers) {
        int count = nearbyLockers.size();
        int[] parent = new int[count];
        for (int index = 0; index < count; index++) {
            parent[index] = index;
        }

        connectBySharedKeys(nearbyLockers, parent, count);
        return collectGroups(nearbyLockers, parent, count);
    }

    private void connectBySharedKeys(List<NearbyLocker> nearbyLockers, int[] parent, int count) {
        // 좌표/도로명 주소/클러스터별 대표 인덱스를 기억해 같은 조건이면 union 한다.
        Map<NearbyLockerCoordinateKey, Integer> coordinateRepresentative = new LinkedHashMap<>();
        Map<String, Integer> roadAddressRepresentative = new LinkedHashMap<>();
        Map<Integer, Integer> clusterRepresentative = new LinkedHashMap<>();

        for (int index = 0; index < count; index++) {
            NearbyLocker locker = nearbyLockers.get(index);

            // 소수점 6자리 반올림 좌표가 같으면 같은 그룹으로 본다.
            NearbyLockerCoordinateKey coordinateKey = coordinateKey(locker.latitude(), locker.longitude());
            Integer coordinateOwner = coordinateRepresentative.putIfAbsent(coordinateKey, index);
            if (coordinateOwner != null) {
                union(parent, coordinateOwner, index);
            }

            // 도로명 주소가 같으면 같은 그룹으로 본다.
            String roadAddressKey = roadAddressKey(locker.roadAddress());
            if (roadAddressKey != null) {
                Integer roadAddressOwner = roadAddressRepresentative.putIfAbsent(roadAddressKey, index);
                if (roadAddressOwner != null) {
                    union(parent, roadAddressOwner, index);
                }
            }

            // DBSCAN 결과 clusterId가 같으면 같은 그룹으로 본다.
            Integer clusterId = locker.clusterId();
            if (clusterId != null) {
                Integer clusterOwner = clusterRepresentative.putIfAbsent(clusterId, index);
                if (clusterOwner != null) {
                    union(parent, clusterOwner, index);
                }
            }
        }
    }

    private List<List<NearbyLocker>> collectGroups(List<NearbyLocker> nearbyLockers, int[] parent, int count) {
        // union-find 루트별로 다시 모아 최종 그룹 리스트를 만든다.
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
