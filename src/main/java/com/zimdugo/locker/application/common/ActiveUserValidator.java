package com.zimdugo.locker.application.common;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserReader;
import com.zimdugo.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActiveUserValidator {

    private final UserReader userReader;

    public void validate(Long userId) {
        User user = userReader.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException(ErrorCode.USER_ALREADY_WITHDRAWN);
        }
    }
}
