package com.zimdugo.locker.entrypoint;

import com.zimdugo.common.security.CurrentUser;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.FavoriteLockerCommandService;
import com.zimdugo.locker.application.FavoriteLockerQueryService;
import com.zimdugo.locker.application.result.favorite.FavoriteLockerListResult;
import com.zimdugo.locker.entrypoint.dto.response.favorite.FavoriteLockerListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LockerFavoriteController implements LockerFavoriteApi {

    private final FavoriteLockerCommandService favoriteLockerCommandService;
    private final FavoriteLockerQueryService favoriteLockerQueryService;

    @Override
    public ResponseEntity<RestResponse<Void>> addFavoriteLocker(
        @CurrentUser Long userId,
        Long lockerId
    ) {
        favoriteLockerCommandService.add(userId, lockerId);
        return ResponseEntity.ok(RestResponse.ok(SuccessCode.OK));
    }

    @Override
    public ResponseEntity<RestResponse<Void>> removeFavoriteLocker(
        @CurrentUser Long userId,
        Long lockerId
    ) {
        favoriteLockerCommandService.remove(userId, lockerId);
        return ResponseEntity.ok(RestResponse.ok(SuccessCode.OK));
    }

    @Override
    public ResponseEntity<RestResponse<FavoriteLockerListResponse>> getFavoriteLockers(
        @CurrentUser Long userId,
        Double latitude,
        Double longitude,
        int page,
        int size
    ) {
        FavoriteLockerListResult result = favoriteLockerQueryService.getFavoriteLockers(
            userId,
            latitude,
            longitude,
            page,
            size
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, FavoriteLockerListResponse.from(result)));
    }
}
