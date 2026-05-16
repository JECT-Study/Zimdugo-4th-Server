package com.zimdugo.locker.infrastructure;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.FavoriteLockerStore;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.UserLockerFavoriteEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.zimdugo.user.infrastructure.UserRepository;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FavoriteLockerStoreAdapter implements FavoriteLockerStore {

    private final UserLockerFavoriteRepository userLockerFavoriteRepository;
    private final UserRepository userRepository;
    private final LockerRepository lockerRepository;

    @Override
    public void add(Long userId, Long lockerId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        LockerEntity locker = lockerRepository.findByIdAndDeletedFalse(lockerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));
        int displayOrder = nextDisplayOrder(userId);

        try {
            userLockerFavoriteRepository.save(new UserLockerFavoriteEntity(user, locker, displayOrder));
        } catch (DataIntegrityViolationException e) {
            // Concurrent favorite requests may race on the unique constraint.
            if (userLockerFavoriteRepository.existsByUserIdAndLockerIdAndLockerDeletedFalse(userId, lockerId)) {
                return;
            }
            throw e;
        }
    }

    @Override
    public void remove(Long userId, Long lockerId) {
        userLockerFavoriteRepository.deleteByUserIdAndLockerId(userId, lockerId);
    }

    @Override
    public void reorder(Long userId, List<Long> lockerIds) {
        long favoriteCount = userLockerFavoriteRepository.countByUserIdAndLockerDeletedFalse(userId);
        if (favoriteCount != lockerIds.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        List<UserLockerFavoriteEntity> favorites =
            userLockerFavoriteRepository.findByUserIdAndLockerDeletedFalseAndLockerIdIn(
                userId,
                lockerIds
            );
        if (favorites.size() != lockerIds.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        Map<Long, UserLockerFavoriteEntity> favoriteByLockerId = new HashMap<>();
        for (UserLockerFavoriteEntity favorite : favorites) {
            favoriteByLockerId.put(favorite.getLocker().getId(), favorite);
        }

        for (int index = 0; index < lockerIds.size(); index++) {
            Long lockerId = lockerIds.get(index);
            UserLockerFavoriteEntity favorite = favoriteByLockerId.remove(lockerId);
            if (favorite == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }
            favorite.updateDisplayOrder(index);
        }
    }

    private int nextDisplayOrder(Long userId) {
        return userLockerFavoriteRepository.findTopByUserIdAndLockerDeletedFalseOrderByDisplayOrderDesc(userId)
            .map(UserLockerFavoriteEntity::getDisplayOrder)
            .map(order -> order + 1)
            .orElse(0);
    }
}
