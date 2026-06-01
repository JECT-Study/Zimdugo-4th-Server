package com.zimdugo.locker.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.zimdugo.locker.application.result.report.LockerReportCreateResult;
import com.zimdugo.locker.domain.LockerReportCreateInfo;
import com.zimdugo.locker.domain.LockerReportStore;
import com.zimdugo.locker.domain.SavedLockerReport;
import com.zimdugo.locker.infrastructure.persistence.GroundLevelType;
import com.zimdugo.locker.infrastructure.persistence.IndoorOutdoorType;
import com.zimdugo.locker.infrastructure.persistence.LockerSizeType;
import com.zimdugo.locker.infrastructure.persistence.LockerType;
import java.time.LocalTime;
import java.util.List;
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

    private static final String LOCKER_NAME = "\uBB3C\uD488\uBCF4\uAD00\uD568";
    private static final String ROAD_ADDRESS = "\uC11C\uC6B8 \uB9C8\uD3EC\uAD6C \uC591\uD654\uB85C 160";
    private static final String ADDITIONAL_INFO = "B2 \uD654\uC7A5\uC2E4 \uC606";

    @Mock
    private LockerReportStore lockerReportStore;

    @InjectMocks
    private LockerReportCommandService lockerReportCommandService;

    @Nested
    @DisplayName("\uBCF4\uAD00\uD568 \uC81C\uBCF4 \uB4F1\uB85D")
    class Create {

        @Test
        @DisplayName("\uC2E0\uADDC \uC81C\uBCF4\uBA74 report\uB97C \uC800\uC7A5\uD55C\uB2E4")
        void createNewReport() {
            given(lockerReportStore.create(any(LockerReportCreateInfo.class)))
                .willReturn(testReport());

            LockerReportCreateResult result = lockerReportCommandService.create(1L, createNewCommand());

            assertThat(result.reportId()).isEqualTo(100L);
            assertThat(result.name()).isEqualTo(LOCKER_NAME);
            assertThat(result.reportStatus()).isEqualTo("SUBMITTED");
            verify(lockerReportStore).create(any(LockerReportCreateInfo.class));
        }

        @Test
        @DisplayName("\uC81C\uBCF4 \uC785\uB825 typed field\uB97C \uADF8\uB300\uB85C \uC804\uB2EC\uD55C\uB2E4")
        void saveReportWithTypedFields() {
            given(lockerReportStore.create(any(LockerReportCreateInfo.class)))
                .willReturn(testReport());

            lockerReportCommandService.create(1L, createCommand());

            ArgumentCaptor<LockerReportCreateInfo> captor =
                ArgumentCaptor.forClass(LockerReportCreateInfo.class);
            verify(lockerReportStore).create(captor.capture());

            LockerReportCreateInfo createInfo = captor.getValue();
            assertThat(createInfo.userId()).isEqualTo(1L);
            assertThat(createInfo.name()).isEqualTo(LOCKER_NAME);
            assertThat(createInfo.roadAddress()).isEqualTo(ROAD_ADDRESS);
            assertThat(createInfo.groundLevelType()).isEqualTo(GroundLevelType.UNDERGROUND);
            assertThat(createInfo.floorNumber()).isEqualTo(2);
            assertThat(createInfo.indoorOutdoorType()).isEqualTo(IndoorOutdoorType.INDOOR);
            assertThat(createInfo.lockerType()).isEqualTo(LockerType.SUBWAY_STATION);
            assertThat(createInfo.lockerSize()).containsExactlyInAnyOrder(LockerSizeType.SMALL, LockerSizeType.MEDIUM);
            assertThat(createInfo.isFree()).isFalse();
            assertThat(createInfo.minPrice()).isEqualTo(1000);
            assertThat(createInfo.maxPrice()).isEqualTo(3000);
            assertThat(createInfo.startTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(createInfo.endTime()).isEqualTo(LocalTime.of(22, 30));
            assertThat(createInfo.additionalInfo()).isEqualTo(ADDITIONAL_INFO);
            assertThat(createInfo.locationConsentAgreed()).isTrue();
            assertThat(createInfo.latitude()).isEqualTo(37.556);
            assertThat(createInfo.longitude()).isEqualTo(126.923);
        }
    }

    private SavedLockerReport testReport() {
        return new SavedLockerReport(100L, "SUBMITTED");
    }

    private LockerReportCreateCommand createCommand() {
        return new LockerReportCreateCommand(
            ROAD_ADDRESS,
            37.556,
            126.923,
            true,
            "UNDERGROUND",
            2,
            "INDOOR",
            "SUBWAY_STATION",
            List.of("SMALL", "MEDIUM"),
            false,
            1000,
            3000,
            LocalTime.of(9, 0),
            LocalTime.of(22, 30),
            ADDITIONAL_INFO,
            "https://cdn.example.com/locker/1.jpg",
            true
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
            "INDOOR",
            "SUBWAY_STATION",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            true
        );
    }
}
