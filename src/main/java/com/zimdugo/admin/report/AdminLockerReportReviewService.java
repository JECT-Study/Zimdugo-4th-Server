package com.zimdugo.admin.report;

import com.zimdugo.admin.report.dto.AdminLockerReportApprovalCommand;
import com.zimdugo.admin.report.dto.AdminLockerReportReviewPageResult;
import com.zimdugo.admin.report.dto.AdminLockerReportReviewPageResult.ExistingPlace;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReportRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLockerReportReviewService {

    private static final int PLACE_CANDIDATE_RADIUS_METERS = 30;
    private final LockerReportRepository reportRepository;
    private final PlaceRepository placeRepository;
    private final PlaceCandidateProvider candidateProvider;
    private final AdminLockerReportApprovalWriter approvalWriter;

    @Transactional(readOnly = true)
    public AdminLockerReportReviewPageResult getReviewPage(Long reportId) {
        LockerReportEntity report = requireReport(reportId);
        List<ExistingPlace> existingPlaces = placeRepository.findAdminCandidates(
                report.getRoadAddress(),
                report.getLatitude(),
                report.getLongitude(),
                PLACE_CANDIDATE_RADIUS_METERS
            ).stream()
            .map(ExistingPlace::from)
            .toList();
        try {
            List<KakaoPlaceCandidate> kakaoPlaces = candidateProvider.findNearby(
                report.getLatitude(), report.getLongitude()
            );
            return new AdminLockerReportReviewPageResult(
                AdminLockerReportReviewPageResult.Report.from(report),
                existingPlaces,
                kakaoPlaces,
                null
            );
        } catch (BusinessException exception) {
            return new AdminLockerReportReviewPageResult(
                AdminLockerReportReviewPageResult.Report.from(report),
                existingPlaces,
                List.of(),
                exception.getMessage()
            );
        }
    }

    public void approve(Long reportId, AdminLockerReportApprovalCommand command, String reviewer) {
        approvalWriter.approve(reportId, command, reviewer);
    }

    public void reject(Long reportId, String rejectionMemo, String reviewer) {
        approvalWriter.reject(reportId, rejectionMemo, reviewer);
    }

    private LockerReportEntity requireReport(Long reportId) {
        return reportRepository.findActiveByIdWithImage(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_REPORT_NOT_FOUND));
    }

}
