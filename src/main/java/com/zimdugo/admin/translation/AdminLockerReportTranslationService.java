package com.zimdugo.admin.translation;

import com.zimdugo.admin.entrypoint.dto.AdminLockerReportTranslationsForm;
import com.zimdugo.admin.i18n.LockerContentI18nAdminService;
import com.zimdugo.admin.i18n.dto.AdminLockerI18nResponse;
import com.zimdugo.admin.i18n.dto.AdminPlaceI18nResponse;
import com.zimdugo.admin.report.LockerReportApprovedEvent;
import com.zimdugo.admin.translation.dto.AdminLockerReportSummaryResult;
import com.zimdugo.admin.translation.dto.AdminLockerReportTranslationPageResult;
import com.zimdugo.admin.translation.dto.AdminTranslationDraftResult;
import com.zimdugo.admin.translation.dto.LockerReportTranslationSource;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.exception.ExternalApiException;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.locker.domain.report.LockerReportStatus;
import com.zimdugo.locker.infrastructure.persistence.LockerReportRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLockerReportTranslationService {

    private static final int REPORT_LIST_SIZE = 50;

    private final LockerReportRepository lockerReportRepository;
    private final PlaceRepository placeRepository;
    private final LockerRepository lockerRepository;
    private final LockerContentI18nAdminService i18nAdminService;
    private final LockerReportTranslationDraftGenerator draftGenerator;
    private final ApplicationEventPublisher eventPublisher;

    public List<AdminLockerReportSummaryResult> getRecentReports() {
        PageRequest pageRequest = PageRequest.of(0, REPORT_LIST_SIZE);

        return lockerReportRepository.findRecentForAdminReportList(pageRequest).stream()
            .map(AdminLockerReportSummaryResult::from)
            .toList();
    }

    public AdminLockerReportTranslationPageResult getTranslationPage(Long reportId) {
        LockerReportEntity report = requireReport(reportId);
        PlaceEntity place = requireAppliedPlace(report);
        LockerEntity locker = requireAppliedLocker(report);
        AdminPlaceI18nResponse placeI18n = i18nAdminService.getPlace(place.getId());
        AdminLockerI18nResponse lockerI18n = i18nAdminService.getLocker(locker.getId());

        return AdminLockerReportTranslationPageResult.of(
            report,
            place.getName(),
            locker.getName(),
            placeI18n,
            lockerI18n,
            hasAllTranslations(placeI18n.translations().size()),
            hasAllTranslations(lockerI18n.translations().size())
        );
    }

    public AdminTranslationDraftResult generateDraft(Long reportId) {
        return draftGenerator.generate(translationSource(reportId));
    }

    public AdminTranslationDraftResult generateDraft(
        Long reportId,
        SupportedLanguage language
    ) {
        AdminTranslationDraftResult draft = draftGenerator.generate(
            translationSource(reportId),
            language
        );
        if (draft.placeTranslationFor(language.languageTag()) == null
            || draft.lockerTranslationFor(language.languageTag()) == null) {
            throw new ExternalApiException("요청한 언어의 번역 응답이 없습니다.");
        }
        return draft;
    }

    private LockerReportTranslationSource translationSource(Long reportId) {
        LockerReportEntity report = requireReport(reportId);
        PlaceEntity place = requireAppliedPlace(report);
        LockerEntity locker = requireAppliedLocker(report);
        return LockerReportTranslationSource.from(report, place, locker);
    }

    @Transactional
    public void saveTranslations(Long reportId, AdminLockerReportTranslationsForm form) {
        LockerReportEntity report = requireReportForUpdate(reportId);
        if (report.getStatus() != LockerReportStatus.TRANSLATION_REQUIRED
            && report.getStatus() != LockerReportStatus.READY_FOR_APPROVAL) {
            throw new BusinessException(ErrorCode.LOCKER_REPORT_ALREADY_REVIEWED);
        }
        PlaceEntity place = requireAppliedPlace(report);
        LockerEntity locker = requireAppliedLocker(report);
        i18nAdminService.replacePlace(place.getId(), form.toPlaceRequest());
        i18nAdminService.replaceLocker(locker.getId(), form.toLockerRequest());
        report.markTranslationsReady();
    }

    @Transactional
    public void completeApproval(Long reportId) {
        LockerReportEntity report = requireReportForUpdate(reportId);
        if (report.getStatus() != LockerReportStatus.READY_FOR_APPROVAL) {
            throw new BusinessException(ErrorCode.LOCKER_REPORT_ALREADY_REVIEWED);
        }
        PlaceEntity place = requireAppliedPlace(report);
        LockerEntity locker = requireAppliedLocker(report);
        AdminPlaceI18nResponse placeI18n = i18nAdminService.getPlace(place.getId());
        AdminLockerI18nResponse lockerI18n = i18nAdminService.getLocker(locker.getId());
        if (!hasAllTranslations(placeI18n.translations().size())
            || !hasAllTranslations(lockerI18n.translations().size())) {
            throw new BusinessException(ErrorCode.LOCKER_REPORT_TRANSLATION_INCOMPLETE);
        }
        place.activate();
        locker.activate();
        report.completeApproval();
        eventPublisher.publishEvent(new LockerReportApprovedEvent(report.getAppliedLockerId()));
    }

    private LockerReportEntity requireReport(Long reportId) {
        return lockerReportRepository.findActiveByIdWithImage(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_REPORT_NOT_FOUND));
    }

    private LockerReportEntity requireReportForUpdate(Long reportId) {
        return lockerReportRepository.findByIdForUpdate(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_REPORT_NOT_FOUND));
    }

    private PlaceEntity requireAppliedPlace(LockerReportEntity report) {
        if (report.getAppliedPlaceId() == null) {
            throw new BusinessException(ErrorCode.PLACE_NOT_FOUND);
        }
        return placeRepository.findById(report.getAppliedPlaceId())
            .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
    }

    private LockerEntity requireAppliedLocker(LockerReportEntity report) {
        if (report.getAppliedLockerId() == null) {
            throw new BusinessException(ErrorCode.LOCKER_NOT_FOUND);
        }
        return lockerRepository.findById(report.getAppliedLockerId())
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));
    }

    private boolean hasAllTranslations(int translationCount) {
        return translationCount == SupportedLanguage.translationTargets().size();
    }
}
