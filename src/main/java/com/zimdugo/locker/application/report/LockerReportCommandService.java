package com.zimdugo.locker.application.report;

import com.zimdugo.locker.application.common.ActiveUserValidator;

import com.zimdugo.locker.application.result.report.LockerReportCreateResult;
import com.zimdugo.locker.domain.report.LockerReportStore;
import com.zimdugo.locker.domain.report.SavedLockerReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LockerReportCommandService {

    private final ActiveUserValidator activeUserValidator;
    private final LockerReportStore lockerReportStore;

    public LockerReportCreateResult create(Long userId, LockerReportCreateCommand command) {
        activeUserValidator.validate(userId);
        SavedLockerReport report = lockerReportStore.create(command.toCreateInfo(userId));
        log.info(
            "보관함 제보 생성 완료. userId={}, reportId={}, locationConsentAgreed={}",
            userId,
            report.id(),
            command.locationConsentAgreed()
        );

        return LockerReportCreateResult.of(report, command);
    }
}
