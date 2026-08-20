package com.zimdugo.locker.application.issue;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.result.issue.LockerIssueReportCreateResult;
import com.zimdugo.locker.domain.issue.LockerIssueReportStore;
import com.zimdugo.locker.domain.issue.SavedLockerIssueReport;
import com.zimdugo.locker.domain.locker.LockerReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockerIssueReportCommandService {

    private final LockerReader lockerReader;
    private final LockerIssueReportStore lockerIssueReportStore;

    @Transactional
    public LockerIssueReportCreateResult create(LockerIssueReportCreateCommand command) {
        if (!lockerReader.existsById(command.lockerId())) {
            throw new BusinessException(ErrorCode.LOCKER_NOT_FOUND);
        }

        SavedLockerIssueReport report = lockerIssueReportStore.create(command.toCreateInfo());
        log.info(
            "보관함 신고 생성 완료. lockerId={}, reportId={}, reportType={}",
            report.lockerId(),
            report.id(),
            report.reportType()
        );
        return LockerIssueReportCreateResult.from(report);
    }
}
