package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.FavoriteLockerReader;
import com.zimdugo.locker.domain.FavoriteLockerStore;
import com.zimdugo.locker.domain.LockerReader;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.domain.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteLockerCommandService {

    private final FavoriteLockerStore favoriteLockerStore;
    private final FavoriteLockerReader favoriteLockerReader;
    private final LockerReader lockerReader;
    private final UserReader userReader;

    public void add(Long userId, Long lockerId) {
        validateUser(userId);
        if (!lockerReader.existsById(lockerId)) {
            throw new BusinessException(ErrorCode.LOCKER_NOT_FOUND);
        }
        if (favoriteLockerReader.exists(userId, lockerId)) {
            return;
        }
        favoriteLockerStore.save(userId, lockerId);
    }

    public void remove(Long userId, Long lockerId) {
        validateUser(userId);
        favoriteLockerStore.delete(userId, lockerId);
    }

    private void validateUser(Long userId) {
        User user = userReader.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException(ErrorCode.USER_ALREADY_WITHDRAWN);
        }
    }
}
