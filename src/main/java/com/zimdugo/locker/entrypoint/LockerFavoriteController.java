package com.zimdugo.locker.entrypoint;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.FavoriteLockerCommandService;
import com.zimdugo.locker.application.FavoriteLockerQueryService;
import com.zimdugo.locker.application.FavoriteLockerResponse;
import com.zimdugo.locker.application.FavoriteLockerStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LockerFavoriteController implements LockerFavoriteApi {

    private final FavoriteLockerQueryService favoriteLockerQueryService;
    private final FavoriteLockerCommandService favoriteLockerCommandService;

    @Override
    public ResponseEntity<RestResponse<FavoriteLockerResponse>> getMyFavoriteLockers(
        Authentication authentication,
        int page,
        int size,
        Double latitude,
        Double longitude
    ) {
        FavoriteLockerResponse response = favoriteLockerQueryService.getFavorites(
            extractUserId(authentication),
            page,
            size,
            latitude,
            longitude
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, response));
    }

    @Override
    public ResponseEntity<RestResponse<FavoriteLockerStatusResponse>> getFavoriteLockerStatus(
        Authentication authentication,
        Long lockerId
    ) {
        FavoriteLockerStatusResponse response = favoriteLockerQueryService.getFavoriteStatus(
            extractUserId(authentication),
            lockerId
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, response));
    }

    @Override
    public ResponseEntity<RestResponse<Void>> reorderFavoriteLockers(
        Authentication authentication,
        FavoriteLockerOrderUpdateRequest request
    ) {
        favoriteLockerCommandService.reorder(extractUserId(authentication), request.lockerIds());
        return ResponseEntity.ok(RestResponse.ok(SuccessCode.OK));
    }

    @Override
    public ResponseEntity<RestResponse<Void>> addFavoriteLocker(
        Authentication authentication,
        Long lockerId
    ) {
        favoriteLockerCommandService.add(extractUserId(authentication), lockerId);
        return ResponseEntity.ok(RestResponse.ok(SuccessCode.OK));
    }

    @Override
    public ResponseEntity<RestResponse<Void>> removeFavoriteLocker(
        Authentication authentication,
        Long lockerId
    ) {
        favoriteLockerCommandService.remove(extractUserId(authentication), lockerId);
        return ResponseEntity.ok(RestResponse.ok(SuccessCode.OK));
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATED_USER_NOT_FOUND);
        }

        return Long.valueOf(authentication.getName());
    }
}
