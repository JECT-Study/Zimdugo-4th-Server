package com.zimdugo.locker.entrypoint;

import com.zimdugo.common.security.NullableCurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.detail.LockerDetailQueryService;
import com.zimdugo.locker.application.keyword.LockerKeywordQueryService;
import com.zimdugo.locker.application.pin.LockerPinQueryService;
import com.zimdugo.locker.application.suggest.LockerSuggestQueryService;
import com.zimdugo.locker.application.place.PlaceLockerQueryService;
import com.zimdugo.locker.application.seo.LockerSeoQueryService;
import com.zimdugo.locker.application.result.detail.LockerDetailResult;
import com.zimdugo.locker.application.result.keyword.LockerKeywordResult;
import com.zimdugo.locker.application.result.pin.LockerPinResult;
import com.zimdugo.locker.application.result.place.PlaceLockerResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestResult;
import com.zimdugo.locker.application.result.seo.LockerSeoResult;
import com.zimdugo.locker.entrypoint.dto.request.keyword.LockerKeywordRequest;
import com.zimdugo.locker.entrypoint.dto.request.place.PlaceLockerRequest;
import com.zimdugo.locker.entrypoint.dto.response.detail.LockerDetailResponse;
import com.zimdugo.locker.entrypoint.dto.response.keyword.LockerKeywordResponse;
import com.zimdugo.locker.entrypoint.dto.response.pin.LockerPinResponse;
import com.zimdugo.locker.entrypoint.dto.response.place.PlaceLockerResponse;
import com.zimdugo.locker.entrypoint.dto.response.suggest.LockerSuggestResponse;
import com.zimdugo.locker.entrypoint.dto.response.seo.LockerSeoListResponse;
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

    private final LockerDetailQueryService lockerDetailQueryService;
    private final LockerPinQueryService lockerPinQueryService;
    private final LockerSuggestQueryService lockerSuggestQueryService;
    private final LockerKeywordQueryService lockerKeywordQueryService;
    private final PlaceLockerQueryService placeLockerQueryService;
    private final LockerSeoQueryService lockerSeoQueryService;

    @Override
    public ResponseEntity<RestResponse<LockerDetailResponse>> getLockerDetail(
        @NullableCurrentUser Long userId,
        Long lockerId
    ) {
        LockerDetailResult result = lockerDetailQueryService.getDetail(userId, lockerId);
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, LockerDetailResponse.from(result)));
    }

    @Override
    public ResponseEntity<RestResponse<LockerPinResponse>> getPins(
        Long userId,
        double latitude,
        double longitude,
        int radiusMeters
    ) {
        LockerPinResult result = lockerPinQueryService.getPins(userId, latitude, longitude, radiusMeters);
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
        @NullableCurrentUser Long userId,
        LockerKeywordRequest request
    ) {
        LockerKeywordResult result = lockerKeywordQueryService.getKeywordResults(userId, request.toCommand());
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, LockerKeywordResponse.from(result)));
    }

    @Override
    public ResponseEntity<RestResponse<PlaceLockerResponse>> getPlaceLockers(
        @NullableCurrentUser Long userId,
        Long placeId,
        PlaceLockerRequest request
    ) {
        PlaceLockerResult result = placeLockerQueryService.getPlaceLockers(userId, request.toCommand(placeId));
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, PlaceLockerResponse.from(result)));
    }

    @Override
    public ResponseEntity<RestResponse<LockerSeoListResponse>> getLockerSeoList() {
        List<LockerSeoResult> result = lockerSeoQueryService.getSeoList();
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, LockerSeoListResponse.from(result)));
    }
}
