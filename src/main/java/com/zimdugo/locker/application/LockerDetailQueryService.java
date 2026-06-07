package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.result.detail.LockerDetailResult;
import com.zimdugo.locker.domain.LockerDetail;
import com.zimdugo.locker.domain.LockerDetailReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerDetailQueryService {

    private final LockerDetailReader lockerDetailReader;

    public LockerDetailResult getDetail(Long lockerId) {
        LockerDetail detail = lockerDetailReader.readById(lockerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));
        return LockerDetailResult.from(detail, false);
    }
}
