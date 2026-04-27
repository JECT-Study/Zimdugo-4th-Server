package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.DuplicateHandlingType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockerReportCommandServiceTest {

    private static final String LOCKER_NAME = "Hongdae Exit 2 Test Locker";
    private static final String ROAD_ADDRESS = "160 Yanghwa-ro, Mapo-gu, Seoul";

    @Mock
    private LockerStore lockerStore;

    @Mock
    private LockerReportStore lockerReportStore;

    @InjectMocks
    private LockerReportCommandService lockerReportCommandService;

    @Nested
    @DisplayName("Locker report creation")
    class Create {

        @Test
        @DisplayName("Creates a locker and stores the report for CREATE_NEW")
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
        @DisplayName("Stores the report on an existing locker for ADD_TO_EXISTING")
        void addReportToExistingLocker() {
            given(lockerStore.getById(10L)).willReturn(testLocker());
            given(lockerReportStore.create(any(LockerReportCreateInfo.class)))
                .willReturn(testReport());

            lockerReportCommandService.create(1L, addToExistingCommand());

            ArgumentCaptor<LockerReportCreateInfo> captor =
                ArgumentCaptor.forClass(LockerReportCreateInfo.class);
            verify(lockerStore, never()).create(any(), any(), anyDouble(), anyDouble());
            verify(lockerReportStore).create(captor.capture());
            assertThat(captor.getValue().lockerId()).isEqualTo(10L);
            assertThat(captor.getValue().duplicateHandlingType())
                .isEqualTo(DuplicateHandlingType.ADD_TO_EXISTING);
            assertThat(captor.getValue().roadAddress()).isEqualTo(ROAD_ADDRESS);
        }
    }

    private ReportLocker testLocker() {
        return new ReportLocker(10L, LOCKER_NAME, ROAD_ADDRESS, 37.556, 126.923);
    }

    private SavedLockerReport testReport() {
        return new SavedLockerReport(100L, "COMPLETED");
    }

    private LockerReportCreateCommand createNewCommand() {
        return LockerReportCreateCommand.of(
            "CREATE_NEW",
            null,
            LOCKER_NAME,
            ROAD_ADDRESS,
            null,
            "Hongdae Station",
            null,
            null,
            "UNKNOWN",
            null,
            null,
            null,
            null,
            37.556,
            126.923
        );
    }

    private LockerReportCreateCommand addToExistingCommand() {
        return LockerReportCreateCommand.of(
            "ADD_TO_EXISTING",
            10L,
            LOCKER_NAME,
            ROAD_ADDRESS,
            null,
            "Hongdae Station",
            null,
            null,
            "UNKNOWN",
            null,
            null,
            null,
            null,
            37.556,
            126.923
        );
    }
}
