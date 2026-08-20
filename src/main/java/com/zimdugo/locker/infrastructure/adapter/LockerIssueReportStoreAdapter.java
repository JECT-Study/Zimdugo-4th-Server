package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.issue.LockerIssueReportCreateInfo;
import com.zimdugo.locker.domain.issue.LockerIssueReportStore;
import com.zimdugo.locker.domain.issue.SavedLockerIssueReport;
import com.zimdugo.locker.infrastructure.persistence.LockerIssueReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerIssueReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockerIssueReportStoreAdapter implements LockerIssueReportStore {

    private final LockerIssueReportRepository lockerIssueReportRepository;

    @Override
    public SavedLockerIssueReport create(LockerIssueReportCreateInfo createInfo) {
        LockerIssueReportEntity entity = lockerIssueReportRepository.save(
            LockerIssueReportEntity.builder()
                .lockerId(createInfo.lockerId())
                .reportType(createInfo.reportType())
                .detail(createInfo.detail())
                .status(createInfo.status())
                .build()
        );
        return new SavedLockerIssueReport(
            entity.getId(),
            entity.getLockerId(),
            entity.getReportType(),
            entity.getDetail(),
            entity.getStatus(),
            entity.getCreatedAt()
        );
    }
}
