package com.zimdugo.locker.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.LockerNearbyQueryService;
import com.zimdugo.locker.application.NearbyLockerResponse;
import java.util.List;
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

    private final LockerNearbyQueryService lockerNearbyQueryService;

    @Override
    public ResponseEntity<RestResponse<List<NearbyLockerResponse>>> getNearbyLockers(
        double latitude, double longitude, int radiusMeters
    ) {
        List<NearbyLockerResponse> response = lockerNearbyQueryService.getNearbyLockers(
            latitude, longitude, radiusMeters
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, response));
    }
}
