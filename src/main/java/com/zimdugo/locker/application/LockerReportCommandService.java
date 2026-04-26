package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class LockerReportCommandService {

    public LockerReportCreateResult create(Long userId, LockerReportCreateCommand command) {
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
