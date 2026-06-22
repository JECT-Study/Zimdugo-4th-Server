package com.zimdugo.admin.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimdugo.admin.report.dto.AdminLockerReportApprovalCommand;
import com.zimdugo.admin.report.dto.AdminLockerReportReviewPageResult;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.report.LockerReportOperatingTimeType;
import com.zimdugo.locker.domain.report.LockerReportPriceType;
import com.zimdugo.locker.domain.report.LockerReportStatus;
import com.zimdugo.locker.infrastructure.persistence.LockerDetailEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerDetailRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerReportRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceRepository;
import com.zimdugo.locker.infrastructure.projection.AdminPlaceCandidateProjection;
import com.zimdugo.user.domain.UserRole;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminLockerReportReviewServiceTest {

    @Mock private LockerReportRepository reportRepository;
    @Mock private PlaceRepository placeRepository;
    @Mock private LockerRepository lockerRepository;
    @Mock private LockerDetailRepository detailRepository;
    @Mock private PlaceCandidateProvider candidateProvider;

    private AdminLockerReportReviewService service;

    @BeforeEach
    void setUp() {
        AdminLockerReportApprovalWriter approvalWriter = new AdminLockerReportApprovalWriter(
            reportRepository,
            placeRepository,
            lockerRepository,
            detailRepository
        );
        service = new AdminLockerReportReviewService(
            reportRepository,
            placeRepository,
            candidateProvider,
            approvalWriter
        );
    }

    @Test
    void approvalCommandSeparatesPlaceAndLockerNames() {
        assertThat(Arrays.stream(AdminLockerReportApprovalCommand.class.getRecordComponents()))
            .extracting(component -> component.getName())
            .containsSubsequence("placeName", "lockerName")
            .doesNotContain("reviewNote");
    }

    @Test
    void rejectsBlankPlaceNameWhenCreatingPlace() {
        assertThatThrownBy(() -> new AdminLockerReportApprovalCommand(
            null,
            null,
            " ",
            "서울역 1층 2번 출구 물품보관함",
            "서울 중구 한강대로 405",
            37.5547,
            126.9707
        )).isInstanceOf(BusinessException.class);
    }

    @Test
    void getsExistingAndKakaoCandidatesForReview() {
        LockerReportEntity report = report();
        AdminPlaceCandidateProjection existing = existingCandidate();
        KakaoPlaceCandidate kakao = poi();
        when(reportRepository.findActiveByIdWithImage(1L)).thenReturn(Optional.of(report));
        when(placeRepository.findAdminCandidates("서울 중구 세종대로", 37.55, 126.97, 30))
            .thenReturn(List.of(existing));
        when(candidateProvider.findNearby(37.55, 126.97)).thenReturn(List.of(kakao));

        AdminLockerReportReviewPageResult page = service.getReviewPage(1L);

        assertThat(page.existingPlaces()).hasSize(1);
        assertThat(page.kakaoPlaces()).containsExactly(kakao);
        assertThat(page.kakaoError()).isNull();
        assertThat(page.report().floorLabel()).isEqualTo("층 없음");
        assertThat(page.report().priceLabel()).isEqualTo("가격 정보 없음");
        assertThat(page.report().operatingTimeLabel()).isEqualTo("운영 시간 정보 없음");
    }

    @Test
    void keepsReviewPageAvailableWhenKakaoFails() {
        LockerReportEntity report = report();
        when(reportRepository.findActiveByIdWithImage(1L)).thenReturn(Optional.of(report));
        when(placeRepository.findAdminCandidates("서울 중구 세종대로", 37.55, 126.97, 30))
            .thenReturn(List.of());
        when(candidateProvider.findNearby(37.55, 126.97))
            .thenThrow(new BusinessException(ErrorCode.EXTERNAL_API_ERROR));

        AdminLockerReportReviewPageResult page = service.getReviewPage(1L);

        assertThat(page.kakaoPlaces()).isEmpty();
        assertThat(page.kakaoError()).isNotBlank();
    }

    @Test
    void approvesReportWithExistingPlaceAndManualLockerName() {
        LockerReportEntity report = report();
        PlaceEntity place = new PlaceEntity("서울역", 37.55, 126.97, "서울 중구 세종대로");
        LockerEntity savedLocker = org.mockito.Mockito.mock(LockerEntity.class);
        when(savedLocker.getId()).thenReturn(20L);
        when(reportRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(report));
        when(placeRepository.findAdminCandidates("서울 중구 세종대로", 37.55, 126.97, 30))
            .thenReturn(List.of(existingCandidate()));
        when(placeRepository.findById(10L)).thenReturn(Optional.of(place));
        when(lockerRepository.save(any(LockerEntity.class))).thenReturn(savedLocker);

        service.approve(1L, approval(10L), "admin");

        assertThat(report.getStatus()).isEqualTo(LockerReportStatus.TRANSLATION_REQUIRED);
        assertThat(report.getName()).isEqualTo("서울역 물품보관함");
        assertThat(report.getAppliedLockerId()).isEqualTo(20L);
        verify(detailRepository).save(any(LockerDetailEntity.class));
    }

    @Test
    void rejectsExistingPlaceThatIsNotAReviewCandidate() {
        LockerReportEntity report = report();
        when(reportRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(report));
        when(placeRepository.findAdminCandidates("서울 중구 세종대로", 37.55, 126.97, 30))
            .thenReturn(List.of());

        assertThatThrownBy(() -> service.approve(1L, approval(99L), "admin"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void approvesReportByCreatingPlaceFromSeparateManualNames() {
        LockerReportEntity report = report();
        PlaceEntity savedPlace = org.mockito.Mockito.mock(PlaceEntity.class);
        LockerEntity savedLocker = org.mockito.Mockito.mock(LockerEntity.class);
        when(savedPlace.getId()).thenReturn(10L);
        when(savedLocker.getId()).thenReturn(20L);
        when(reportRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(report));
        when(placeRepository.save(any(PlaceEntity.class))).thenReturn(savedPlace);
        when(lockerRepository.save(any(LockerEntity.class))).thenReturn(savedLocker);

        service.approve(1L, approval(null), "admin");

        verify(placeRepository).save(any(PlaceEntity.class));
        assertThat(report.getAppliedPlaceId()).isEqualTo(10L);
        assertThat(report.getAppliedLockerId()).isEqualTo(20L);
    }

    @Test
    void usesSubmittedManualValuesWhenApproving() {
        LockerReportEntity report = report();
        PlaceEntity savedPlace = org.mockito.Mockito.mock(PlaceEntity.class);
        LockerEntity savedLocker = org.mockito.Mockito.mock(LockerEntity.class);
        when(savedPlace.getId()).thenReturn(10L);
        when(savedLocker.getId()).thenReturn(20L);
        when(reportRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(report));
        when(placeRepository.save(any(PlaceEntity.class))).thenReturn(savedPlace);
        when(lockerRepository.save(any(LockerEntity.class))).thenReturn(savedLocker);
        AdminLockerReportApprovalCommand manual = new AdminLockerReportApprovalCommand(
            null,
            null,
            "관리자 입력 장소",
            "관리자 입력 보관함",
            "관리자 입력 주소",
            1.0,
            2.0
        );

        service.approve(1L, manual, "admin");

        ArgumentCaptor<PlaceEntity> placeCaptor = ArgumentCaptor.forClass(PlaceEntity.class);
        ArgumentCaptor<LockerEntity> lockerCaptor = ArgumentCaptor.forClass(LockerEntity.class);
        verify(placeRepository).save(placeCaptor.capture());
        verify(lockerRepository).save(lockerCaptor.capture());
        assertThat(placeCaptor.getValue().getName()).isEqualTo("관리자 입력 장소");
        assertThat(lockerCaptor.getValue().getName()).isEqualTo("관리자 입력 보관함");
        assertThat(report.getName()).isEqualTo("관리자 입력 보관함");
    }

    @Test
    void rejectsReportWithReviewerAndInternalMemo() {
        LockerReportEntity report = report();
        when(reportRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(report));

        service.reject(1L, "중복 제보", "admin");

        assertThat(report.getStatus()).isEqualTo(LockerReportStatus.REJECTED);
        assertThat(report.getReviewedBy()).isEqualTo("admin");
        assertThat(report.getRejectionMemo()).isEqualTo("중복 제보");
    }

    private AdminLockerReportApprovalCommand approval(Long placeId) {
        return new AdminLockerReportApprovalCommand(
            placeId,
            "kakao-1",
            "서울역",
            "서울역 물품보관함",
            "서울 중구 한강대로 405",
            37.5547,
            126.9707
        );
    }

    private KakaoPlaceCandidate poi() {
        return new KakaoPlaceCandidate(
            "kakao-1",
            "서울역 물품보관함",
            "교통 > 철도역",
            "서울 중구 한강대로 405",
            37.5547,
            126.9707,
            12,
            "https://place.map.kakao.com/1"
        );
    }

    private AdminPlaceCandidateProjection existingCandidate() {
        return new AdminPlaceCandidateProjection() {
            public Long getPlaceId() { return 10L; }
            public String getPlaceName() { return "서울역"; }
            public String getRoadAddress() { return "서울 중구 세종대로"; }
            public Double getLatitude() { return 37.55; }
            public Double getLongitude() { return 126.97; }
            public Double getDistanceMeters() { return 5.0; }
            public Boolean getExactAddress() { return true; }
        };
    }

    private LockerReportEntity report() {
        return LockerReportEntity.builder()
            .id(1L)
            .user(user())
            .roadAddress("서울 중구 세종대로")
            .indoorOutdoorType(IndoorOutdoorType.INDOOR)
            .lockerType(LockerType.SUBWAY_STATION)
            .lockerSize(Set.of())
            .priceType(LockerReportPriceType.UNKNOWN)
            .operatingTimeType(LockerReportOperatingTimeType.UNKNOWN)
            .locationConsentAgreed(true)
            .latitude(37.55)
            .longitude(126.97)
            .status(LockerReportStatus.SUBMITTED)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private UserEntity user() {
        return new UserEntity(
            1L,
            "user@example.com",
            "user",
            null,
            UserStatus.ACTIVE,
            UserRole.USER,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
}
