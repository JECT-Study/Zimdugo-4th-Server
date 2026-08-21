package com.zimdugo.admin.issue;

import com.zimdugo.admin.issue.dto.AdminLockerIssueReportDetailResult;
import com.zimdugo.admin.issue.dto.AdminLockerIssueReportSummaryResult;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.locker.domain.issue.LockerIssueReportStatus;
import com.zimdugo.locker.domain.issue.LockerIssueReportType;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerIssueReportEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerIssueReportRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
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

@ExtendWith(MockitoExtension.class)
class AdminLockerIssueReportServiceTest {

    @Mock
    private LockerIssueReportRepository lockerIssueReportRepository;

    @Mock
    private LockerRepository lockerRepository;

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
            LocalDateTime.of(2026, 8, 20, 23, 30, 0),
            null,
            null,
            null
        ));
        LockerEntity locker = locker(1L, "서울역 보관함", "서울 중구 세종대로 1");
        given(lockerIssueReportRepository.findAllByStatusOrderByCreatedAtDesc(LockerIssueReportStatus.PENDING))
            .willReturn(List.of(report));
        given(lockerRepository.findAllById(List.of(1L))).willReturn(List.of(locker));

        List<AdminLockerIssueReportSummaryResult> result =
            adminLockerIssueReportService.getReports(LockerIssueReportStatus.PENDING.name());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).lockerName()).isEqualTo("서울역 보관함");
        assertThat(result.get(0).status()).isEqualTo(LockerIssueReportStatus.PENDING);
    }

    @Test
    @DisplayName("상태 필터가 없으면 전체 신고를 조회한다")
    void getReportsWithoutStatus() {
        LockerIssueReportEntity report = issueReport(new IssueReportFixture(
            13L,
            4L,
            LockerIssueReportType.CATEGORY_ERROR,
            null,
            LockerIssueReportStatus.RESOLVED,
            LocalDateTime.of(2026, 8, 21, 0, 10, 0),
            "admin",
            "카테고리를 수정했습니다.",
            LocalDateTime.of(2026, 8, 21, 0, 20, 0)
        ));
        LockerEntity locker = locker(4L, "홍대입구역 보관함", "서울 마포구 양화로 3");
        given(lockerIssueReportRepository.findAllByOrderByCreatedAtDesc()).willReturn(List.of(report));
        given(lockerRepository.findAllById(List.of(4L))).willReturn(List.of(locker));

        List<AdminLockerIssueReportSummaryResult> result = adminLockerIssueReportService.getReports(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).reviewedAt()).isEqualTo(LocalDateTime.of(2026, 8, 21, 0, 20, 0));
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
            LocalDateTime.of(2026, 8, 20, 23, 31, 0),
            "admin",
            "이미지를 최신 사진으로 교체했습니다.",
            LocalDateTime.of(2026, 8, 20, 23, 45, 0)
        ));
        LockerEntity locker = locker(2L, "강남역 보관함", "서울 강남구 강남대로 2");
        given(lockerIssueReportRepository.findById(11L)).willReturn(Optional.of(report));
        given(lockerRepository.findById(2L)).willReturn(Optional.of(locker));

        AdminLockerIssueReportDetailResult result = adminLockerIssueReportService.getReport(11L);

        assertThat(result.lockerName()).isEqualTo("강남역 보관함");
        assertThat(result.reportType()).isEqualTo(LockerIssueReportType.IMAGE_ERROR);
        assertThat(result.status()).isEqualTo(LockerIssueReportStatus.RESOLVED);
        assertThat(result.reviewedBy()).isEqualTo("admin");
        assertThat(result.reviewNote()).isEqualTo("이미지를 최신 사진으로 교체했습니다.");
    }

    @Test
    @DisplayName("이미 처리된 신고는 다시 처리할 수 없다")
    void rejectAlreadyReviewedReport() {
        LockerIssueReportEntity report = issueReport(new IssueReportFixture(
            12L,
            3L,
            LockerIssueReportType.OTHER,
            null,
            LockerIssueReportStatus.REJECTED,
            LocalDateTime.of(2026, 8, 20, 23, 32, 0),
            "admin",
            "중복 신고입니다.",
            LocalDateTime.of(2026, 8, 20, 23, 40, 0)
        ));
        given(lockerIssueReportRepository.findById(12L)).willReturn(Optional.of(report));

        assertThatThrownBy(() -> adminLockerIssueReportService.resolve(12L, "다시 처리", "admin"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("신고를 처리 완료하면 처리자와 메모를 남긴다")
    void resolveReport() {
        LockerIssueReportEntity report = issueReport(new IssueReportFixture(
            14L,
            5L,
            LockerIssueReportType.OPERATING_HOURS_ERROR,
            "운영 시간이 달라요.",
            LockerIssueReportStatus.PENDING,
            LocalDateTime.of(2026, 8, 21, 1, 0, 0),
            null,
            null,
            null
        ));
        given(lockerIssueReportRepository.findById(14L)).willReturn(Optional.of(report));

        adminLockerIssueReportService.resolve(14L, "운영시간 정보를 수정했습니다.", "reviewer");

        assertThat(report.getStatus()).isEqualTo(LockerIssueReportStatus.RESOLVED);
        assertThat(report.getReviewedBy()).isEqualTo("reviewer");
        assertThat(report.getReviewNote()).isEqualTo("운영시간 정보를 수정했습니다.");
        assertThat(report.getReviewedAt()).isNotNull();
    }

    @Test
    @DisplayName("처리 메모가 비어 있으면 예외가 발생한다")
    void rejectBlankReviewMemo() {
        LockerIssueReportEntity report = issueReport(new IssueReportFixture(
            15L,
            6L,
            LockerIssueReportType.WRONG_LOCATION,
            null,
            LockerIssueReportStatus.PENDING,
            LocalDateTime.of(2026, 8, 21, 1, 10, 0),
            null,
            null,
            null
        ));
        given(lockerIssueReportRepository.findById(15L)).willReturn(Optional.of(report));

        assertThatThrownBy(() -> adminLockerIssueReportService.reject(15L, "  ", "reviewer"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("삭제된 보관함의 신고도 상세 조회할 수 있다")
    void getReportForDeletedLocker() {
        LockerIssueReportEntity report = issueReport(new IssueReportFixture(
            16L,
            7L,
            LockerIssueReportType.SIZE_ERROR,
            "대형이 아닙니다.",
            LockerIssueReportStatus.PENDING,
            LocalDateTime.of(2026, 8, 21, 1, 20, 0),
            null,
            null,
            null
        ));
        given(lockerIssueReportRepository.findById(16L)).willReturn(Optional.of(report));
        given(lockerRepository.findById(7L)).willReturn(Optional.empty());

        AdminLockerIssueReportDetailResult result = adminLockerIssueReportService.getReport(16L);

        assertThat(result.lockerName()).isEqualTo("삭제된 보관함");
        assertThat(result.lockerDeleted()).isTrue();
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
        setField(entity, "reviewedBy", fixture.reviewedBy());
        setField(entity, "reviewNote", fixture.reviewNote());
        setField(entity, "reviewedAt", fixture.reviewedAt());
        return entity;
    }

    private LockerEntity locker(Long id, String name, String roadAddress) {
        LockerEntity entity = new LockerEntity(name, roadAddress, 37.0, 127.0);
        setField(entity, "id", id);
        return entity;
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
        LocalDateTime createdAt,
        String reviewedBy,
        String reviewNote,
        LocalDateTime reviewedAt
    ) {
    }
}
