package com.zimdugo.admin.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.admin.report.dto.AdminLockerReportApprovalCommand;
import com.zimdugo.admin.report.dto.AdminLockerReportReviewPageResult;
import com.zimdugo.admin.report.dto.AdminLockerReportReviewPageResult.ExistingPlace;
import com.zimdugo.admin.report.dto.AdminLockerReportReviewPageResult.ImageMetadata;
import com.zimdugo.admin.report.dto.AdminLockerReportReviewPageResult.MetadataEntry;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReportRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
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
    private final LockerRepository lockerRepository;
    private final ObjectMapper objectMapper;
    private final PlaceCandidateProvider candidateProvider;
    private final AdminLockerReportApprovalWriter approvalWriter;

    @Transactional(readOnly = true)
    public AdminLockerReportReviewPageResult getReviewPage(Long reportId) {
        LockerReportEntity report = requireReport(reportId);
        List<ExistingPlace> existingPlaces = findExistingPlaces(report);
        String appliedPlaceName = findAppliedPlaceName(report);
        String appliedLockerName = findAppliedLockerName(report);
        ImageMetadata imageMetadata = ImageMetadata.from(report.getImage(), parseMetadataEntries(report));
        Double imageDistanceMeters = findImageDistanceMeters(report);
        AdminLockerReportReviewPageResult.Report reportResult =
            AdminLockerReportReviewPageResult.Report.from(
                report, appliedPlaceName, appliedLockerName, imageMetadata, imageDistanceMeters
            );
        try {
            List<KakaoPlaceCandidate> kakaoPlaces = candidateProvider.findNearby(
                report.getLatitude(), report.getLongitude()
            );
            return new AdminLockerReportReviewPageResult(
                reportResult, existingPlaces, kakaoPlaces, null
            );
        } catch (BusinessException exception) {
            return new AdminLockerReportReviewPageResult(
                reportResult, existingPlaces, List.of(), exception.getMessage()
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

    private List<ExistingPlace> findExistingPlaces(LockerReportEntity report) {
        return placeRepository.findAdminCandidates(
                report.getRoadAddress(),
                report.getLatitude(),
                report.getLongitude(),
                PLACE_CANDIDATE_RADIUS_METERS
            ).stream()
            .map(ExistingPlace::from)
            .toList();
    }

    private String findAppliedPlaceName(LockerReportEntity report) {
        if (report.getAppliedPlaceId() == null) {
            return null;
        }
        return placeRepository.findById(report.getAppliedPlaceId())
            .map(PlaceEntity::getName)
            .orElse(null);
    }

    private String findAppliedLockerName(LockerReportEntity report) {
        if (report.getAppliedLockerId() == null) {
            return null;
        }
        return lockerRepository.findById(report.getAppliedLockerId())
            .map(LockerEntity::getName)
            .orElse(null);
    }

    private List<MetadataEntry> parseMetadataEntries(LockerReportEntity report) {
        if (report.getImage() == null || report.getImage().getExifMetadataJson() == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                report.getImage().getExifMetadataJson(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, MetadataEntry.class)
            );
        } catch (Exception exception) {
            return List.of();
        }
    }

    private Double findImageDistanceMeters(LockerReportEntity report) {
        if (report.getImage() == null
            || report.getImage().getGpsLatitude() == null
            || report.getImage().getGpsLongitude() == null) {
            return null;
        }
        return reportRepository.findImageDistanceMeters(report.getId()).orElse(null);
    }

}
