package com.zimdugo.locker.application.detail;

import com.zimdugo.common.i18n.CurrentRequestLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.result.detail.LockerDetailResult;
import com.zimdugo.locker.domain.detail.LockerDetail;
import com.zimdugo.locker.domain.detail.LockerDetailReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerDetailQueryService {

    private final LockerDetailReader lockerDetailReader;
    private final CurrentRequestLanguage currentRequestLanguage;

    public LockerDetailResult getDetail(Long userId, Long lockerId) {
        String languageCode = currentRequestLanguage.resolve().languageTag();
        LockerDetail detail = lockerDetailReader.readById(lockerId, userId, languageCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));

        return LockerDetailResult.from(detail);
    }
}
