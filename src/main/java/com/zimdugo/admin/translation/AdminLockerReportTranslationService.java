package com.zimdugo.admin.translation;

import com.zimdugo.admin.i18n.LockerContentI18nAdminService;
import com.zimdugo.admin.i18n.dto.AdminLockerI18nResponse;
import com.zimdugo.admin.i18n.dto.AdminPlaceI18nResponse;
import com.zimdugo.admin.translation.dto.AdminLockerReportSummaryResult;
import com.zimdugo.admin.translation.dto.AdminLockerReportTranslationPageResult;
import com.zimdugo.admin.translation.dto.AdminTranslationDraftResult;
import com.zimdugo.admin.translation.dto.LockerReportTranslationSource;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReportRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLockerReportTranslationService {

    private static final int REPORT_LIST_SIZE = 50;

    private final LockerReportRepository lockerReportRepository;
    private final LockerContentI18nAdminService i18nAdminService;
    private final LockerReportTranslationDraftGenerator draftGenerator;

    public List<AdminLockerReportSummaryResult> getRecentReports() {
        PageRequest pageRequest = PageRequest.of(
            0,
            REPORT_LIST_SIZE,
            Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        return lockerReportRepository.findAll(pageRequest).stream()
            .map(AdminLockerReportSummaryResult::from)
            .toList();
    }

    public AdminLockerReportTranslationPageResult getTranslationPage(Long reportId) {
        LockerReportEntity report = requireReport(reportId);
        AdminPlaceI18nResponse placeI18n = report.getAppliedPlaceId() == null
            ? null
            : i18nAdminService.getPlace(report.getAppliedPlaceId());
        AdminLockerI18nResponse lockerI18n = report.getAppliedLockerId() == null
            ? null
            : i18nAdminService.getLocker(report.getAppliedLockerId());

        return AdminLockerReportTranslationPageResult.of(report, placeI18n, lockerI18n);
    }

    public AdminTranslationDraftResult generateDraft(Long reportId) {
        LockerReportEntity report = requireReport(reportId);
        return draftGenerator.generate(LockerReportTranslationSource.from(report));
    }

    private LockerReportEntity requireReport(Long reportId) {
        return lockerReportRepository.findActiveByIdWithImage(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_REPORT_NOT_FOUND));
    }
}
