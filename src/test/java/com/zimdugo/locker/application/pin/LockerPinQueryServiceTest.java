package com.zimdugo.locker.application.pin;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.result.pin.LockerPinItemResult;
import com.zimdugo.locker.application.result.pin.LockerPinResult;
import com.zimdugo.locker.domain.favorite.FavoriteLockerReader;
import com.zimdugo.locker.domain.locker.NearbyLocker;
import com.zimdugo.locker.domain.locker.NearbyLockerPlaceReader;
import java.util.List;
import java.util.Set;
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
class LockerPinQueryServiceTest {

    @Mock
    private NearbyLockerPlaceReader nearbyLockerPlaceReader;

    @Mock
    private LockerPinAssembler lockerPinAssembler;

    @Mock
    private FavoriteLockerReader favoriteLockerReader;

    @InjectMocks
    private LockerPinQueryService lockerPinQueryService;

    @Test
    @DisplayName("주변 보관함이 없으면 빈 결과를 반환한다")
    void returnsEmptyWhenNoNearbyLockers() {
        given(nearbyLockerPlaceReader.findNearby(37.55, 126.93, 500)).willReturn(List.of());

        LockerPinResult result = lockerPinQueryService.getPins(1L, 37.55, 126.93, 500);

        assertThat(result.count()).isZero();
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("주변 보관함이 있으면 assembler 결과를 반환한다")
    void returnsAssemblerResultWhenNearbyLockersExist() {
        List<NearbyLocker> nearbyLockers = List.of(sampleLocker());
        List<LockerPinItemResult> pins = List.of(
            LockerPinItemResult.locker(1L, 37.55, 126.93, true)
        );

        given(nearbyLockerPlaceReader.findNearby(37.55, 126.93, 500)).willReturn(nearbyLockers);
        given(favoriteLockerReader.findFavoriteLockerIds(1L, Set.of(1L))).willReturn(Set.of(1L));
        given(lockerPinAssembler.assemble(nearbyLockers, Set.of(1L))).willReturn(pins);

        LockerPinResult result = lockerPinQueryService.getPins(1L, 37.55, 126.93, 500);

        assertThat(result.count()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        verify(lockerPinAssembler).assemble(nearbyLockers, Set.of(1L));
    }

    @Test
    @DisplayName("좌표가 범위를 벗어나면 주변 보관함을 조회하지 않는다")
    void doesNotReadNearbyLockersWhenLocationIsOutOfRange() {
        assertThatThrownBy(() -> lockerPinQueryService.getPins(1L, 90.1, 126.93, 500))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_LOCATION_RANGE);

        verify(nearbyLockerPlaceReader, never()).findNearby(90.1, 126.93, 500);
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
