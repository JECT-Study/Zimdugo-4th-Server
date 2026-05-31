package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
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

    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    private final NearbyLockerPlaceReader nearbyLockerPlaceReader;
    private final LockerPinAssembler lockerPinAssembler;

    public LockerPinResult getPins(double latitude, double longitude, int radiusMeters) {
        validateLocation(latitude, longitude);

        List<NearbyLocker> nearbyLockers = nearbyLockerPlaceReader.findNearby(latitude, longitude, radiusMeters);
        if (nearbyLockers.isEmpty()) {
            return LockerPinResult.empty();
        }

        List<LockerPinItemResult> pins = lockerPinAssembler.assemble(nearbyLockers);
        return LockerPinResult.of(pins);
    }

    private void validateLocation(double lat, double lon) {
        if (lat < MIN_LATITUDE || lat > MAX_LATITUDE || lon < MIN_LONGITUDE || lon > MAX_LONGITUDE) {
            throw new BusinessException(ErrorCode.INVALID_LOCATION_RANGE);
        }
    }
}
