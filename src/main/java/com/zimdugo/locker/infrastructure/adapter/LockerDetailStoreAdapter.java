package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.detail.LockerDetail;
import com.zimdugo.locker.domain.detail.LockerDetailStore;
import com.zimdugo.locker.infrastructure.persistence.LockerDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class LockerDetailStoreAdapter implements LockerDetailStore {

    private final LockerDetailRepository lockerDetailRepository;

    @Override
    public void save(LockerDetail lockerDetail) {
        lockerDetailRepository.findByLockerId(lockerDetail.lockerId())
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND))
            .updateVoteCounts(lockerDetail.accurateVoteCount(), lockerDetail.inaccurateVoteCount());
    }
}
