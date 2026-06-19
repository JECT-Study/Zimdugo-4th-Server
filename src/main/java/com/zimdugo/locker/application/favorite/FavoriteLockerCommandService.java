package com.zimdugo.locker.application.favorite;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.favorite.FavoriteLockerReader;
import com.zimdugo.locker.domain.favorite.FavoriteLockerStore;
import com.zimdugo.locker.domain.locker.LockerReader;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.domain.UserReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
            log.debug("이미 등록된 즐겨찾기 요청입니다. userId={}, lockerId={}", userId, lockerId);
            return;
        }
        favoriteLockerStore.save(userId, lockerId);
        log.info("즐겨찾기 등록 완료. userId={}, lockerId={}", userId, lockerId);
    }

    public void remove(Long userId, Long lockerId) {
        validateUser(userId);
        favoriteLockerStore.delete(userId, lockerId);
        log.info("즐겨찾기 삭제 요청 처리 완료. userId={}, lockerId={}", userId, lockerId);
    }

    private void validateUser(Long userId) {
        User user = userReader.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException(ErrorCode.USER_ALREADY_WITHDRAWN);
        }
    }
}
