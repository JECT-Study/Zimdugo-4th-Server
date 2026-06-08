package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.result.detail.LockerDetailResult;
import com.zimdugo.locker.domain.FavoriteLockerReader;
import com.zimdugo.locker.domain.LockerDetail;
import com.zimdugo.locker.domain.LockerDetailReader;
import com.zimdugo.locker.domain.LockerVoteReader;
import com.zimdugo.locker.domain.LockerVoteType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerDetailQueryService {

    private final LockerDetailReader lockerDetailReader;
    private final FavoriteLockerReader favoriteLockerReader;
    private final LockerVoteReader lockerVoteReader;

    public LockerDetailResult getDetail(Long userId, Long lockerId) {
        LockerDetail detail = lockerDetailReader.readById(lockerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));

        boolean isFavorite = false;
        boolean isAccurateVoted = false;
        boolean isInaccurateVoted = false;

        if (userId != null) {
            isFavorite = favoriteLockerReader.exists(userId, lockerId);
            isAccurateVoted = lockerVoteReader.exists(userId, lockerId, LockerVoteType.CORRECT);
            isInaccurateVoted = lockerVoteReader.exists(userId, lockerId, LockerVoteType.INCORRECT);
        }

        return LockerDetailResult.from(detail, isFavorite, isAccurateVoted, isInaccurateVoted);
    }
}
