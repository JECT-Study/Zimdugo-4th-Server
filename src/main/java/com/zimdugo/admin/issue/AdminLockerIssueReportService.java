package com.zimdugo.admin.issue;

import com.zimdugo.admin.issue.dto.AdminLockerIssueReportDetailResult;
import com.zimdugo.admin.issue.dto.AdminLockerIssueReportReviewCommand;
import com.zimdugo.admin.issue.dto.AdminLockerIssueReportStatusOption;
import com.zimdugo.admin.issue.dto.AdminLockerIssueReportSummaryResult;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.issue.LockerIssueReportStatus;
import com.zimdugo.locker.infrastructure.persistence.LockerIssueReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerIssueReportLockerRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerIssueReportRepository;
import com.zimdugo.locker.infrastructure.projection.LockerIssueReportLockerProjection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLockerIssueReportService {

    private final LockerIssueReportRepository lockerIssueReportRepository;
    private final LockerIssueReportLockerRepository lockerIssueReportLockerRepository;

    public List<AdminLockerIssueReportStatusOption> getStatusOptions() {
        return java.util.Arrays.stream(LockerIssueReportStatus.values())
            .map(status -> new AdminLockerIssueReportStatusOption(status.name(), status.displayName()))
            .toList();
    }

    public List<AdminLockerIssueReportSummaryResult> getReports(String statusCode) {
        LockerIssueReportStatus status = parseStatus(statusCode);
        List<LockerIssueReportEntity> reports = status == null
            ? lockerIssueReportRepository.findAllByOrderByCreatedAtDesc()
            : lockerIssueReportRepository.findAllByStatusOrderByCreatedAtDesc(status);

        if (reports.isEmpty()) {
            return List.of();
        }

        Map<Long, LockerIssueReportLockerProjection> lockers = lockerIssueReportLockerRepository.findLockersByIds(
            reports.stream().map(LockerIssueReportEntity::getLockerId).distinct().toList()
        )
            .stream()
            .collect(java.util.stream.Collectors.toMap(
                LockerIssueReportLockerProjection::getId,
                Function.identity()
            ));

        return reports.stream()
            .map(report -> toSummaryResult(report, lockers.get(report.getLockerId())))
            .toList();
    }

    public AdminLockerIssueReportDetailResult getReport(Long reportId) {
        LockerIssueReportEntity report = getEntity(reportId);
        LockerIssueReportLockerProjection locker =
            lockerIssueReportLockerRepository.findLockerById(report.getLockerId())
                .orElse(null);
        return toDetailResult(report, locker);
    }

    @Transactional
    public void resolve(AdminLockerIssueReportReviewCommand command) {
        getEntity(command.reportId()).resolve(command.reviewer(), command.reviewMemo());
    }

    @Transactional
    public void reject(AdminLockerIssueReportReviewCommand command) {
        getEntity(command.reportId()).reject(command.reviewer(), command.reviewMemo());
    }

    private LockerIssueReportStatus parseStatus(String statusCode) {
        if (statusCode == null || statusCode.isBlank()) {
            return null;
        }
        try {
            return LockerIssueReportStatus.valueOf(statusCode);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }
    }

    private LockerIssueReportEntity getEntity(Long reportId) {
        return lockerIssueReportRepository.findById(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_ISSUE_REPORT_NOT_FOUND));
    }

    private AdminLockerIssueReportSummaryResult toSummaryResult(
        LockerIssueReportEntity report,
        LockerIssueReportLockerProjection locker
    ) {
        return new AdminLockerIssueReportSummaryResult(
            report.getId(),
            report.getLockerId(),
            lockerName(locker),
            locker == null ? null : locker.getRoadAddress(),
            locker != null && locker.isDeleted(),
            report.getReportType(),
            report.getDetail(),
            report.getStatus(),
            report.getCreatedAt()
        );
    }

    private AdminLockerIssueReportDetailResult toDetailResult(
        LockerIssueReportEntity report,
        LockerIssueReportLockerProjection locker
    ) {
        boolean lockerDeleted = locker != null && locker.isDeleted();
        boolean lockerManageable = locker != null && !lockerDeleted;
        return new AdminLockerIssueReportDetailResult(
            report.getId(),
            report.getLockerId(),
            lockerName(locker),
            locker == null ? null : locker.getRoadAddress(),
            lockerDeleted,
            lockerManageable,
            report.getReportType(),
            report.getDetail(),
            report.getStatus(),
            report.getCreatedAt(),
            report.getUpdatedAt(),
            report.getReviewMemo(),
            report.getReviewedBy(),
            report.getReviewedAt()
        );
    }

    private String lockerName(LockerIssueReportLockerProjection locker) {
        if (locker == null) {
            return "알 수 없는 보관함";
        }
        return locker.getName();
    }
}
