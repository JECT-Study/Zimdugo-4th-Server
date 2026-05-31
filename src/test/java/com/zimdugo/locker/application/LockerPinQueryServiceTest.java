package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.pin.LockerPinItemResult;
import com.zimdugo.locker.application.result.pin.LockerPinResult;
import com.zimdugo.locker.domain.NearbyLocker;
import com.zimdugo.locker.domain.NearbyLockerPlaceReader;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockerPinQueryServiceTest {

    @Mock
    private NearbyLockerPlaceReader nearbyLockerPlaceReader;

    @Mock
    private LockerPinAssembler lockerPinAssembler;

    @InjectMocks
    private LockerPinQueryService lockerPinQueryService;

    @Test
    @DisplayName("주변 보관함이 없으면 빈 결과를 반환한다")
    void returnsEmptyWhenNoNearbyLockers() {
        given(nearbyLockerPlaceReader.findNearby(37.55, 126.93, 500)).willReturn(List.of());

        LockerPinResult result = lockerPinQueryService.getPins(37.55, 126.93, 500);

        assertThat(result.count()).isZero();
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("주변 보관함이 있으면 assembler 결과를 반환한다")
    void returnsAssemblerResultWhenNearbyLockersExist() {
        List<NearbyLocker> nearbyLockers = List.of(sampleLocker());
        List<LockerPinItemResult> pins = List.of(
            LockerPinItemResult.locker(1L, 37.55, 126.93)
        );

        given(nearbyLockerPlaceReader.findNearby(37.55, 126.93, 500)).willReturn(nearbyLockers);
        given(lockerPinAssembler.assemble(nearbyLockers)).willReturn(pins);

        LockerPinResult result = lockerPinQueryService.getPins(37.55, 126.93, 500);

        assertThat(result.count()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        verify(lockerPinAssembler).assemble(nearbyLockers);
    }

    private NearbyLocker sampleLocker() {
        return new NearbyLocker(
            1L,
            37.55,
            126.93,
            1L
        );
    }
}
