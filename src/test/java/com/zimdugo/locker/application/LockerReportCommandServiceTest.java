package com.zimdugo.locker.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.zimdugo.locker.application.result.report.LockerReportCreateResult;
import com.zimdugo.locker.domain.LockerReportCreateInfo;
import com.zimdugo.locker.domain.LockerReportStore;
import com.zimdugo.locker.domain.LockerStore;
import com.zimdugo.locker.domain.ReportLocker;
import com.zimdugo.locker.domain.SavedLockerReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LockerReportCommandServiceTest {

    private static final String LOCKER_NAME = "물품보관함";
    private static final String ROAD_ADDRESS = "서울 마포구 양화로 160";

    @Mock
    private LockerStore lockerStore;

    @Mock
    private LockerReportStore lockerReportStore;

    @InjectMocks
    private LockerReportCommandService lockerReportCommandService;

    @Nested
    @DisplayName("보관함 제보 등록")
    class Create {

        @Test
        @DisplayName("신규 제보면 보관함을 생성하고 제보 이력을 저장한다")
        void createNewLockerAndReport() {
            given(lockerStore.create(LOCKER_NAME, ROAD_ADDRESS, 37.556, 126.923))
                .willReturn(testLocker());
            given(lockerReportStore.create(any(LockerReportCreateInfo.class)))
                .willReturn(testReport());

            LockerReportCreateResult result = lockerReportCommandService.create(1L, createNewCommand());

            assertThat(result.reportId()).isEqualTo(100L);
            assertThat(result.lockerId()).isEqualTo(10L);
            verify(lockerStore).create(LOCKER_NAME, ROAD_ADDRESS, 37.556, 126.923);
            verify(lockerReportStore).create(any(LockerReportCreateInfo.class));
        }

        @Test
        @DisplayName("제보 저장 시 생성된 보관함 id를 사용한다")
        void saveReportWithCreatedLockerId() {
            given(lockerStore.create(LOCKER_NAME, ROAD_ADDRESS, 37.556, 126.923))
                .willReturn(testLocker());
            given(lockerReportStore.create(any(LockerReportCreateInfo.class)))
                .willReturn(testReport());

            lockerReportCommandService.create(1L, createCommand());

            ArgumentCaptor<LockerReportCreateInfo> captor =
                ArgumentCaptor.forClass(LockerReportCreateInfo.class);
            verify(lockerStore).create(LOCKER_NAME, ROAD_ADDRESS, 37.556, 126.923);
            verify(lockerReportStore).create(captor.capture());
            assertThat(captor.getValue().lockerId()).isEqualTo(10L);
            assertThat(captor.getValue().roadAddress()).isEqualTo(ROAD_ADDRESS);
        }
    }

    private ReportLocker testLocker() {
        return new ReportLocker(10L, LOCKER_NAME, ROAD_ADDRESS, 37.556, 126.923);
    }

    private SavedLockerReport testReport() {
        return new SavedLockerReport(100L, "COMPLETED");
    }

    private LockerReportCreateCommand createCommand() {
        return new LockerReportCreateCommand(
            ROAD_ADDRESS,
            37.556,
            126.923,
            false,
            null,
            null,
            null,
            "UNKNOWN",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false
        );
    }

    private LockerReportCreateCommand createNewCommand() {
        return new LockerReportCreateCommand(
            ROAD_ADDRESS,
            37.556,
            126.923,
            false,
            null,
            null,
            null,
            "UNKNOWN",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false
        );
    }
}
