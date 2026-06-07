package com.zimdugo.locker.entrypoint;

import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.LockerKeywordQueryService;
import com.zimdugo.locker.application.LockerPinQueryService;
import com.zimdugo.locker.application.LockerSuggestQueryService;
import com.zimdugo.locker.application.result.keyword.LockerKeywordResult;
import com.zimdugo.locker.application.result.pin.LockerPinResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestResult;
import com.zimdugo.locker.entrypoint.dto.request.keyword.LockerKeywordRequest;
import com.zimdugo.locker.entrypoint.dto.response.keyword.LockerKeywordResponse;
import com.zimdugo.locker.entrypoint.dto.response.pin.LockerPinResponse;
import com.zimdugo.locker.entrypoint.dto.response.suggest.LockerSuggestResponse;
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
    private final LockerSuggestQueryService lockerSuggestQueryService;
    private final LockerKeywordQueryService lockerKeywordQueryService;

    @Override
    public ResponseEntity<RestResponse<LockerPinResponse>> getPins(
        double latitude,
        double longitude,
        int radiusMeters
    ) {
        LockerPinResult result = lockerPinQueryService.getPins(latitude, longitude, radiusMeters);
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, LockerPinResponse.from(result)));
    }

    @Override
    public ResponseEntity<RestResponse<LockerSuggestResponse>> getSuggestions(
        double latitude,
        double longitude,
        String keyword
    ) {
        LockerSuggestResult result = lockerSuggestQueryService.getSuggestions(
            latitude,
            longitude,
            keyword
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, LockerSuggestResponse.from(result)));
    }

    @Override
    public ResponseEntity<RestResponse<LockerKeywordResponse>> getKeywordResults(
        LockerKeywordRequest request
    ) {
        LockerKeywordResult result = lockerKeywordQueryService.getKeywordResults(request.toCommand());
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, LockerKeywordResponse.from(result)));
    }
}
