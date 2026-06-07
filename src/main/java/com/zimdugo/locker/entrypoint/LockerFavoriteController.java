package com.zimdugo.locker.entrypoint;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import com.zimdugo.locker.application.FavoriteLockerCommandService;
import com.zimdugo.locker.application.FavoriteLockerQueryService;
import com.zimdugo.locker.application.result.favorite.FavoriteLockerListResult;
import com.zimdugo.locker.entrypoint.dto.response.favorite.FavoriteLockerListResponse;
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

    private final FavoriteLockerCommandService favoriteLockerCommandService;
    private final FavoriteLockerQueryService favoriteLockerQueryService;

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

    @Override
    public ResponseEntity<RestResponse<FavoriteLockerListResponse>> getFavoriteLockers(
        Authentication authentication,
        Double latitude,
        Double longitude,
        int page,
        int size
    ) {
        FavoriteLockerListResult result = favoriteLockerQueryService.getFavoriteLockers(
            extractUserId(authentication),
            latitude,
            longitude,
            page,
            size
        );
        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, FavoriteLockerListResponse.from(result)));
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATED_USER_NOT_FOUND);
        }

        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.AUTHENTICATED_USER_NOT_FOUND);
        }
    }
}
