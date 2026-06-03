package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.FavoriteLockerReader;
import com.zimdugo.locker.domain.FavoriteLockerStore;
import com.zimdugo.locker.domain.LockerStore;
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
    private final LockerStore lockerStore;
    private final UserReader userReader;

    public void add(Long userId, Long lockerId) {
        userReader.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        lockerStore.getById(lockerId);
        if (favoriteLockerReader.exists(userId, lockerId)) {
            return;
        }
        favoriteLockerStore.save(userId, lockerId);
    }

    public void remove(Long userId, Long lockerId) {
        favoriteLockerStore.delete(userId, lockerId);
    }
}
