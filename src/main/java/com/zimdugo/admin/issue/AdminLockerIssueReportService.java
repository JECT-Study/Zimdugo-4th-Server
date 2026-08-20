package com.zimdugo.admin.issue;

import com.zimdugo.admin.issue.dto.AdminLockerIssueReportDetailResult;
import com.zimdugo.admin.issue.dto.AdminLockerIssueReportStatusOption;
import com.zimdugo.admin.issue.dto.AdminLockerIssueReportSummaryResult;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.issue.LockerIssueReportStatus;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerIssueReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerIssueReportRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
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
    private final LockerRepository lockerRepository;

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

        Map<Long, LockerEntity> lockers = lockerRepository.findAllById(
            reports.stream().map(LockerIssueReportEntity::getLockerId).distinct().toList()
        ).stream().collect(java.util.stream.Collectors.toMap(LockerEntity::getId, Function.identity()));

        return reports.stream()
            .map(report -> toSummaryResult(report, lockers.get(report.getLockerId())))
            .toList();
    }

    public AdminLockerIssueReportDetailResult getReport(Long reportId) {
        LockerIssueReportEntity report = getEntity(reportId);
        LockerEntity locker = lockerRepository.findById(report.getLockerId())
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));
        return toDetailResult(report, locker);
    }

    @Transactional
    public void resolve(Long reportId) {
        getEntity(reportId).resolve();
    }

    @Transactional
    public void reject(Long reportId) {
        getEntity(reportId).reject();
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
        LockerEntity locker
    ) {
        return new AdminLockerIssueReportSummaryResult(
            report.getId(),
            report.getLockerId(),
            locker == null ? "삭제된 보관함" : locker.getName(),
            locker == null ? null : locker.getRoadAddress(),
            report.getReportType(),
            report.getDetail(),
            report.getStatus(),
            report.getCreatedAt()
        );
    }

    private AdminLockerIssueReportDetailResult toDetailResult(
        LockerIssueReportEntity report,
        LockerEntity locker
    ) {
        return new AdminLockerIssueReportDetailResult(
            report.getId(),
            report.getLockerId(),
            locker.getName(),
            locker.getRoadAddress(),
            report.getReportType(),
            report.getDetail(),
            report.getStatus(),
            report.getCreatedAt(),
            report.getUpdatedAt()
        );
    }
}
