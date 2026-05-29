package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.pin.LockerPinItemResult;
import com.zimdugo.locker.domain.NearbyLocker;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LockerPinAssembler {

    public List<LockerPinItemResult> assemble(List<NearbyLocker> lockers) {
        if (lockers.isEmpty()) {
            return List.of();
        }

        Map<Long, PlaceGroup> groups = groupByPlace(lockers);
        List<LockerPinItemResult> pins = new ArrayList<>(groups.size());
        for (PlaceGroup group : groups.values()) {
            pins.add(group.toPin());
        }
        return pins;
    }

    private Map<Long, PlaceGroup> groupByPlace(List<NearbyLocker> lockers) {
        Map<Long, PlaceGroup> groups = new LinkedHashMap<>();
        for (NearbyLocker locker : lockers) {
            groups.computeIfAbsent(locker.placeId(), ignored -> new PlaceGroup(locker.placeId()))
                .add(locker);
        }
        return groups;
    }

    private static final class PlaceGroup {
        private final Long placeId;
        private final List<NearbyLocker> lockers = new ArrayList<>();

        private PlaceGroup(Long placeId) {
            this.placeId = placeId;
        }

        private void add(NearbyLocker locker) {
            lockers.add(locker);
        }

        private LockerPinItemResult toPin() {
            if (lockers.size() == 1) {
                NearbyLocker locker = lockers.getFirst();
                return LockerPinItemResult.locker(locker.id(), locker.latitude(), locker.longitude());
            }

            double latitudeSum = 0d;
            double longitudeSum = 0d;
            for (NearbyLocker locker : lockers) {
                latitudeSum += locker.latitude();
                longitudeSum += locker.longitude();
            }
            return LockerPinItemResult.place(
                placeId,
                latitudeSum / lockers.size(),
                longitudeSum / lockers.size()
            );
        }
    }
}
