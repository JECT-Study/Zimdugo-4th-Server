package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.result.favorite.FavoriteLockerListItemResult;
import com.zimdugo.locker.application.result.favorite.FavoriteLockerListResult;
import com.zimdugo.locker.domain.FavoriteLockerListItem;
import com.zimdugo.locker.domain.FavoriteLockerListPage;
import com.zimdugo.locker.domain.FavoriteLockerQueryReader;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserReader;
import com.zimdugo.user.domain.UserStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteLockerQueryService {

    static final double DEFAULT_LATITUDE = 37.498095;
    static final double DEFAULT_LONGITUDE = 127.027610;

    private final FavoriteLockerQueryReader favoriteLockerQueryReader;
    private final UserReader userReader;

    public FavoriteLockerListResult getFavoriteLockers(
        Long userId,
        Double latitude,
        Double longitude,
        int page,
        int size
    ) {
        validateUser(userId);

        double resolvedLatitude = latitude == null ? DEFAULT_LATITUDE : latitude;
        double resolvedLongitude = longitude == null ? DEFAULT_LONGITUDE : longitude;

        FavoriteLockerListPage result = favoriteLockerQueryReader.findAll(
            userId,
            resolvedLatitude,
            resolvedLongitude,
            page,
            size
        );

        if (result.items().isEmpty()) {
            return FavoriteLockerListResult.empty();
        }

        List<FavoriteLockerListItemResult> items = result.items().stream()
            .map(this::toResult)
            .toList();

        return FavoriteLockerListResult.of(items, result.totalCount(), result.hasNext());
    }

    private FavoriteLockerListItemResult toResult(FavoriteLockerListItem item) {
        return new FavoriteLockerListItemResult(
            item.lockerId(),
            item.lockerName(),
            item.roadAddress(),
            item.lockerType(),
            item.latitude(),
            item.longitude(),
            item.distanceMeters(),
            item.updatedAt(),
            true
        );
    }

    private void validateUser(Long userId) {
        User user = userReader.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException(ErrorCode.USER_ALREADY_WITHDRAWN);
        }
    }
}
