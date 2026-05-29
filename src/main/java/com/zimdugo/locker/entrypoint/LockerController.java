package com.zimdugo.locker.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.LockerPinQueryService;
import com.zimdugo.locker.application.result.pin.LockerPinResult;
import com.zimdugo.locker.entrypoint.dto.response.pin.LockerPinResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LockerController implements LockerApi {

    private final LockerPinQueryService lockerPinQueryService;

    @Override
    public ResponseEntity<RestResponse<LockerPinResponse>> getPins(
        double latitude,
        double longitude,
        int radiusMeters
    ) {
        LockerPinResult result = lockerPinQueryService.getPins(latitude, longitude, radiusMeters);
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, LockerPinResponse.from(result)));
    }
}
