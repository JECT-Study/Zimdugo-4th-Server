package com.zimdugo.locker.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.zimdugo.locker.application.result.report.LockerReportCreateResult;
import com.zimdugo.locker.domain.LockerReportCreateInfo;
import com.zimdugo.locker.domain.LockerReportNameResolver;
import com.zimdugo.locker.domain.LockerReportStore;
import com.zimdugo.locker.domain.SavedLockerReport;
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

    private static final String LOCKER_NAME = "물품보관함";
    private static final String ROAD_ADDRESS = "서울 마포구 양화로 160";
    private static final String ADDITIONAL_INFO = "B2 출구 근처";

    @Mock
    private LockerReportStore lockerReportStore;

    @Mock
    private ActiveUserValidator activeUserValidator;

    @Mock
    private LockerReportNameResolver lockerReportNameResolver;

    @InjectMocks
    private LockerReportCommandService lockerReportCommandService;

    @Nested
    @DisplayName("보관함 제보 등록")
    class Create {

        @Test
        @DisplayName("신규 제보면 report를 저장한다")
        void createNewReport() {
            given(lockerReportNameResolver.resolve(ROAD_ADDRESS, "SUBWAY_STATION", 37.556, 126.923))
                .willReturn(LOCKER_NAME);
            given(lockerReportStore.create(any(LockerReportCreateInfo.class)))
                .willReturn(testReport());

            LockerReportCreateResult result = lockerReportCommandService.create(1L, createNewCommand());

            assertThat(result.reportId()).isEqualTo(100L);
            assertThat(result.name()).isEqualTo(LOCKER_NAME);
            assertThat(result.reportStatus()).isEqualTo("SUBMITTED");
            verify(lockerReportStore).create(any(LockerReportCreateInfo.class));
        }

        @Test
        @DisplayName("제보 입력 typed field를 그대로 전달한다")
        void saveReportWithTypedFields() {
            given(lockerReportNameResolver.resolve(ROAD_ADDRESS, "SUBWAY_STATION", 37.556, 126.923))
                .willReturn(LOCKER_NAME);
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
            assertThat(createInfo.groundLevelType()).isEqualTo("UNDERGROUND");
            assertThat(createInfo.floorNumber()).isEqualTo(2);
            assertThat(createInfo.indoorOutdoorType()).isEqualTo("INDOOR");
            assertThat(createInfo.lockerType()).isEqualTo("SUBWAY_STATION");
            assertThat(createInfo.sizeTypes()).containsExactly("SMALL", "MEDIUM");
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
