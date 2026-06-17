package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.report.LockerReportCreateResult;
import com.zimdugo.locker.domain.LockerReportStore;
import com.zimdugo.locker.domain.SavedLockerReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LockerReportCommandService {

    private final ActiveUserValidator activeUserValidator;
    private final LockerReportStore lockerReportStore;

    public LockerReportCreateResult create(Long userId, LockerReportCreateCommand command) {
        activeUserValidator.validate(userId);
        SavedLockerReport report = lockerReportStore.create(command.toCreateInfo(userId));

        return LockerReportCreateResult.of(report, command);
    }
}
