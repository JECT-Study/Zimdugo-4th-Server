package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.FavoriteLockerReader;
import com.zimdugo.locker.domain.FavoriteLockerStore;
import com.zimdugo.locker.domain.LockerReader;
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
        userReader.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!lockerReader.existsById(lockerId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (favoriteLockerReader.exists(userId, lockerId)) {
            return;
        }
        favoriteLockerStore.save(userId, lockerId);
    }

    public void remove(Long userId, Long lockerId) {
        favoriteLockerStore.delete(userId, lockerId);
    }
}
