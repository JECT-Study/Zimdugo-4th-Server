package com.zimdugo.admin.report;

import com.zimdugo.admin.report.dto.AdminLockerReportApprovalCommand;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.infrastructure.persistence.LockerDetailEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerDetailRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReportRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLockerReportApprovalWriter {

    private static final int PLACE_CANDIDATE_RADIUS_METERS = 30;
    private static final int MAX_REJECTION_MEMO_LENGTH = 1000;

    private final LockerReportRepository reportRepository;
    private final PlaceRepository placeRepository;
    private final LockerRepository lockerRepository;
    private final LockerDetailRepository detailRepository;

    @Transactional
    public void approve(
        Long reportId,
        AdminLockerReportApprovalCommand command,
        String reviewer
    ) {
        LockerReportEntity report = requireReportForUpdate(reportId);
        PlaceEntity place = resolvePlace(command, report);
        LockerEntity locker = lockerRepository.save(LockerEntity.draft(
            command.lockerName(),
            command.roadAddress(),
            command.latitude(),
            command.longitude(),
            place
        ));
        detailRepository.save(toLockerDetail(report, locker));
        report.approve(
            command.lockerName(),
            place.getId(),
            locker.getId(),
            reviewer
        );
    }

    @Transactional
    public void reject(Long reportId, String rejectionMemo, String reviewer) {
        if (rejectionMemo == null || rejectionMemo.isBlank()
            || rejectionMemo.length() > MAX_REJECTION_MEMO_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_LOCKER_REPORT_INPUT);
        }
        LockerReportEntity report = requireReportForUpdate(reportId);
        report.reject(reviewer, rejectionMemo);
    }

    private PlaceEntity resolvePlace(
        AdminLockerReportApprovalCommand command,
        LockerReportEntity report
    ) {
        if (command.existingPlaceId() != null) {
            boolean isCandidate = placeRepository.findAdminCandidates(
                    report.getRoadAddress(),
                    report.getLatitude(),
                    report.getLongitude(),
                    PLACE_CANDIDATE_RADIUS_METERS
                ).stream()
                .anyMatch(candidate -> command.existingPlaceId().equals(candidate.getPlaceId()));
            if (!isCandidate) {
                throw new BusinessException(ErrorCode.INVALID_LOCKER_REPORT_INPUT);
            }
            return placeRepository.findById(command.existingPlaceId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
        }
        return placeRepository.save(PlaceEntity.draft(
            command.placeName(),
            command.latitude(),
            command.longitude(),
            command.roadAddress()
        ));
    }

    private LockerDetailEntity toLockerDetail(LockerReportEntity report, LockerEntity locker) {
        return new LockerDetailEntity(
            locker,
            report.getLockerType(),
            report.getIndoorOutdoorType(),
            report.getGroundLevelType(),
            report.getFloor(),
            report.getMinPrice(),
            report.getMaxPrice(),
            report.getLockerSize(),
            report.getAdditionalInfo(),
            report.getStartTime(),
            report.getEndTime(),
            report.getImage() == null ? null : report.getImage().getImageUrl()
        );
    }

    private LockerReportEntity requireReportForUpdate(Long reportId) {
        return reportRepository.findByIdForUpdate(reportId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_REPORT_NOT_FOUND));
    }
}
