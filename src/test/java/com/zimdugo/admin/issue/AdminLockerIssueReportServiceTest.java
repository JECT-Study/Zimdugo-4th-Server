package com.zimdugo.admin.issue;

import com.zimdugo.admin.issue.dto.AdminLockerIssueReportDetailResult;
import com.zimdugo.admin.issue.dto.AdminLockerIssueReportReviewCommand;
import com.zimdugo.admin.issue.dto.AdminLockerIssueReportSummaryResult;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.locker.domain.issue.LockerIssueReportStatus;
import com.zimdugo.locker.domain.issue.LockerIssueReportType;
import com.zimdugo.locker.infrastructure.persistence.LockerIssueReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerIssueReportLockerRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerIssueReportRepository;
import com.zimdugo.locker.infrastructure.projection.LockerIssueReportLockerProjection;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminLockerIssueReportServiceTest {

    @Mock
    private LockerIssueReportRepository lockerIssueReportRepository;

    @Mock
    private LockerIssueReportLockerRepository lockerIssueReportLockerRepository;

    @InjectMocks
    private AdminLockerIssueReportService adminLockerIssueReportService;

    @Test
    @DisplayName("상태로 신고 목록을 필터링한다")
    void getReportsByStatus() {
        LockerIssueReportEntity report = issueReport(new IssueReportFixture(
            10L,
            1L,
            LockerIssueReportType.WRONG_LOCATION,
            "위치가 다릅니다.",
            LockerIssueReportStatus.PENDING,
            LocalDateTime.of(2026, 8, 20, 23, 30, 0)
        ));
        given(lockerIssueReportRepository.findAllByStatusOrderByCreatedAtDesc(LockerIssueReportStatus.PENDING))
            .willReturn(List.of(report));
        given(lockerIssueReportLockerRepository.findLockersByIds(List.of(1L)))
            .willReturn(List.of(locker(1L, "서울역 보관함", "서울 중구 세종대로 1", false)));

        List<AdminLockerIssueReportSummaryResult> result =
            adminLockerIssueReportService.getReports(LockerIssueReportStatus.PENDING.name());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).lockerName()).isEqualTo("서울역 보관함");
        assertThat(result.get(0).lockerDeleted()).isFalse();
        assertThat(result.get(0).status()).isEqualTo(LockerIssueReportStatus.PENDING);
    }

    @Test
    @DisplayName("조건에 맞는 신고가 없으면 빈 목록을 반환한다")
    void getReportsWhenEmpty() {
        given(lockerIssueReportRepository.findAllByStatusOrderByCreatedAtDesc(LockerIssueReportStatus.REJECTED))
            .willReturn(List.of());

        List<AdminLockerIssueReportSummaryResult> result =
            adminLockerIssueReportService.getReports(LockerIssueReportStatus.REJECTED.name());

        assertThat(result).isEmpty();
        verify(lockerIssueReportLockerRepository, never()).findLockersByIds(List.of());
    }

    @Test
    @DisplayName("유효하지 않은 상태 필터는 예외가 발생한다")
    void getReportsWithInvalidStatus() {
        assertThatThrownBy(() -> adminLockerIssueReportService.getReports("INVALID"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("신고 상세를 조회한다")
    void getReport() {
        LockerIssueReportEntity report = issueReport(new IssueReportFixture(
            11L,
            2L,
            LockerIssueReportType.IMAGE_ERROR,
            "이미지가 실제와 다릅니다.",
            LockerIssueReportStatus.RESOLVED,
            LocalDateTime.of(2026, 8, 20, 23, 31, 0)
        ));
        given(lockerIssueReportRepository.findById(11L)).willReturn(Optional.of(report));
        given(lockerIssueReportLockerRepository.findLockerById(2L))
            .willReturn(Optional.of(locker(2L, "강남역 보관함", "서울 강남구 강남대로 2", false)));

        AdminLockerIssueReportDetailResult result = adminLockerIssueReportService.getReport(11L);

        assertThat(result.lockerName()).isEqualTo("강남역 보관함");
        assertThat(result.lockerDeleted()).isFalse();
        assertThat(result.lockerManageable()).isTrue();
        assertThat(result.reportType()).isEqualTo(LockerIssueReportType.IMAGE_ERROR);
        assertThat(result.status()).isEqualTo(LockerIssueReportStatus.RESOLVED);
        assertThat(result.reviewMemo()).isNull();
        assertThat(result.reviewedBy()).isNull();
        assertThat(result.reviewedAt()).isNull();
    }

    @Test
    @DisplayName("처리 완료 시 메모와 처리 정보를 저장한다")
    void resolveStoresReviewInfo() {
        LockerIssueReportEntity report = issueReport(new IssueReportFixture(
            20L,
            5L,
            LockerIssueReportType.WRONG_LOCATION,
            "위치가 다릅니다.",
            LockerIssueReportStatus.PENDING,
            LocalDateTime.of(2026, 8, 20, 23, 40, 0)
        ));
        given(lockerIssueReportRepository.findById(20L)).willReturn(Optional.of(report));

        adminLockerIssueReportService.resolve(new AdminLockerIssueReportReviewCommand(
            20L,
            "위치 정보 수정 완료",
            "admin"
        ));

        assertThat(report.getStatus()).isEqualTo(LockerIssueReportStatus.RESOLVED);
        assertThat(report.getReviewMemo()).isEqualTo("위치 정보 수정 완료");
        assertThat(report.getReviewedBy()).isEqualTo("admin");
        assertThat(report.getReviewedAt()).isNotNull();
    }

    @Test
    @DisplayName("반려 시 공백 메모는 저장하지 않는다")
    void rejectStoresNullForBlankMemo() {
        LockerIssueReportEntity report = issueReport(new IssueReportFixture(
            21L,
            6L,
            LockerIssueReportType.OTHER,
            "기타 이슈",
            LockerIssueReportStatus.PENDING,
            LocalDateTime.of(2026, 8, 20, 23, 41, 0)
        ));
        given(lockerIssueReportRepository.findById(21L)).willReturn(Optional.of(report));

        adminLockerIssueReportService.reject(new AdminLockerIssueReportReviewCommand(
            21L,
            "   ",
            "reviewer"
        ));

        assertThat(report.getStatus()).isEqualTo(LockerIssueReportStatus.REJECTED);
        assertThat(report.getReviewMemo()).isNull();
        assertThat(report.getReviewedBy()).isEqualTo("reviewer");
        assertThat(report.getReviewedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 처리된 신고도 처리 결과를 수정할 수 있다")
    void resolveUpdatesReviewedReport() {
        LockerIssueReportEntity report = issueReport(new IssueReportFixture(
            30L,
            8L,
            LockerIssueReportType.OTHER,
            "기존 신고",
            LockerIssueReportStatus.REJECTED,
            LocalDateTime.of(2026, 8, 20, 23, 45, 0)
        ));
        setField(report, "reviewMemo", "기존 반려 메모");
        setField(report, "reviewedBy", "reviewer-a");
        setField(report, "reviewedAt", LocalDateTime.of(2026, 8, 20, 23, 50, 0));
        given(lockerIssueReportRepository.findById(30L)).willReturn(Optional.of(report));

        adminLockerIssueReportService.resolve(new AdminLockerIssueReportReviewCommand(
            30L,
            "확인 후 처리 완료로 수정",
            "reviewer-b"
        ));

        assertThat(report.getStatus()).isEqualTo(LockerIssueReportStatus.RESOLVED);
        assertThat(report.getReviewMemo()).isEqualTo("확인 후 처리 완료로 수정");
        assertThat(report.getReviewedBy()).isEqualTo("reviewer-b");
        assertThat(report.getReviewedAt()).isNotNull();
    }

    @Test
    @DisplayName("삭제된 보관함 신고도 상세를 조회할 수 있다")
    void getReportWhenLockerDeleted() {
        LockerIssueReportEntity report = issueReport(new IssueReportFixture(
            22L,
            7L,
            LockerIssueReportType.NO_LONGER_OPERATING,
            null,
            LockerIssueReportStatus.PENDING,
            LocalDateTime.of(2026, 8, 20, 23, 42, 0)
        ));
        given(lockerIssueReportRepository.findById(22L)).willReturn(Optional.of(report));
        given(lockerIssueReportLockerRepository.findLockerById(7L))
            .willReturn(Optional.of(locker(7L, "폐업 처리된 보관함", null, true)));

        AdminLockerIssueReportDetailResult result = adminLockerIssueReportService.getReport(22L);

        assertThat(result.lockerName()).isEqualTo("폐업 처리된 보관함");
        assertThat(result.lockerDeleted()).isTrue();
        assertThat(result.lockerManageable()).isFalse();
        assertThat(result.lockerRoadAddress()).isNull();
    }

    private LockerIssueReportEntity issueReport(IssueReportFixture fixture) {
        LockerIssueReportEntity entity = LockerIssueReportEntity.builder()
            .lockerId(fixture.lockerId())
            .reportType(fixture.type())
            .detail(fixture.detail())
            .status(fixture.status())
            .build();
        setField(entity, "id", fixture.id());
        setField(entity, "createdAt", fixture.createdAt());
        setField(entity, "updatedAt", fixture.createdAt());
        return entity;
    }

    private LockerIssueReportLockerProjection locker(Long id, String name, String roadAddress, boolean deleted) {
        return new LockerIssueReportLockerProjection() {
            @Override
            public Long getId() {
                return id;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getRoadAddress() {
                return roadAddress;
            }

            @Override
            public LocalDateTime getDeletedAt() {
                return deleted ? LocalDateTime.of(2026, 8, 24, 18, 40, 0) : null;
            }
        };
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private record IssueReportFixture(
        Long id,
        Long lockerId,
        LockerIssueReportType type,
        String detail,
        LockerIssueReportStatus status,
        LocalDateTime createdAt
    ) {
    }
}
