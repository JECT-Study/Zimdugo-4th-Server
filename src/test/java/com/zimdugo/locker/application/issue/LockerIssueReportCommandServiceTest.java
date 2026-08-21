package com.zimdugo.locker.application.issue;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.locker.domain.issue.LockerIssueReportDuplicateGuard;
import com.zimdugo.locker.application.result.issue.LockerIssueReportCreateResult;
import com.zimdugo.locker.domain.issue.LockerIssueReportStatus;
import com.zimdugo.locker.domain.issue.LockerIssueReportStore;
import com.zimdugo.locker.domain.issue.LockerIssueReportType;
import com.zimdugo.locker.domain.issue.SavedLockerIssueReport;
import com.zimdugo.locker.domain.locker.LockerReader;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockerIssueReportCommandServiceTest {

    @Mock
    private LockerReader lockerReader;

    @Mock
    private LockerIssueReportDuplicateGuard lockerIssueReportDuplicateGuard;

    @Mock
    private LockerIssueReportStore lockerIssueReportStore;

    @InjectMocks
    private LockerIssueReportCommandService lockerIssueReportCommandService;

    @Test
    @DisplayName("존재하는 보관함에 익명 신고를 생성한다")
    void createIssueReport() {
        LockerIssueReportCreateCommand command = new LockerIssueReportCreateCommand(
            1L,
            "OPERATING_HOURS_ERROR",
            "운영 시간이 실제와 다릅니다."
        );
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 19, 20, 30, 15);
        SavedLockerIssueReport savedReport = new SavedLockerIssueReport(
            10L,
            1L,
            LockerIssueReportType.OPERATING_HOURS_ERROR,
            "운영 시간이 실제와 다릅니다.",
            LockerIssueReportStatus.PENDING,
            createdAt
        );
        given(lockerReader.existsById(1L)).willReturn(true);
        given(lockerIssueReportStore.create(command.toCreateInfo())).willReturn(savedReport);

        LockerIssueReportCreateResult result = lockerIssueReportCommandService.create(
            command,
            "visitor-1",
            "127.0.0.1"
        );

        assertThat(result.reportId()).isEqualTo(10L);
        assertThat(result.lockerId()).isEqualTo(1L);
        assertThat(result.reportType()).isEqualTo("OPERATING_HOURS_ERROR");
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.createdAt()).isEqualTo(createdAt);
        verify(lockerIssueReportDuplicateGuard).checkAndReserve(1L, "visitor-1", "127.0.0.1");
        verify(lockerIssueReportStore).create(command.toCreateInfo());
    }

    @Test
    @DisplayName("존재하지 않는 보관함이면 신고 생성에 실패한다")
    void failWhenLockerNotFound() {
        LockerIssueReportCreateCommand command = new LockerIssueReportCreateCommand(
            999L,
            "WRONG_LOCATION",
            null
        );
        given(lockerReader.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> lockerIssueReportCommandService.create(command, "visitor-1", "127.0.0.1"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("동일한 방문자가 짧은 시간 내 같은 보관함을 반복 신고하면 생성에 실패한다")
    void failWhenDuplicateIssueReport() {
        LockerIssueReportCreateCommand command = new LockerIssueReportCreateCommand(
            1L,
            "WRONG_LOCATION",
            "위치가 다릅니다."
        );
        given(lockerReader.existsById(1L)).willReturn(true);
        willThrow(new BusinessException(
            com.zimdugo.core.exception.ErrorCode.LOCKER_ISSUE_REPORT_DUPLICATED
        ))
            .given(lockerIssueReportDuplicateGuard)
            .checkAndReserve(1L, "visitor-1", "127.0.0.1");

        assertThatThrownBy(() -> lockerIssueReportCommandService.create(command, "visitor-1", "127.0.0.1"))
            .isInstanceOf(BusinessException.class);
    }
}
