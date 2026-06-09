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
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteLockerQueryService {

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
        UserLocationResolver.ResolvedLocation resolvedLocation = UserLocationResolver.resolve(latitude, longitude);

        FavoriteLockerListPage result = favoriteLockerQueryReader.findAll(
            userId,
            resolvedLocation.latitude(),
            resolvedLocation.longitude(),
            page,
            size
        );

        if (result.items().isEmpty()) {
            return FavoriteLockerListResult.of(
                Collections.emptyList(),
                result.totalCount(),
                result.hasNext()
            );
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
