package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.pin.LockerPinItemResult;
import com.zimdugo.locker.application.result.pin.LockerPinResult;
import com.zimdugo.locker.domain.NearbyLocker;
import com.zimdugo.locker.domain.NearbyLockerPlaceReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerPinQueryService {

    private final NearbyLockerPlaceReader nearbyLockerPlaceReader;
    private final LockerPinAssembler lockerPinAssembler;

    public LockerPinResult getPins(double latitude, double longitude, int radiusMeters) {
        List<NearbyLocker> nearbyLockers = nearbyLockerPlaceReader.findNearby(latitude, longitude, radiusMeters);
        if (nearbyLockers.isEmpty()) {
            return LockerPinResult.empty();
        }

        List<LockerPinItemResult> pins = lockerPinAssembler.assemble(nearbyLockers);
        return LockerPinResult.of(pins);
    }
}
