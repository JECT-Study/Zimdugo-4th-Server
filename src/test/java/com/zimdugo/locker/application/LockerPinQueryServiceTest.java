package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.NearbyLocker;
import com.zimdugo.locker.domain.NearbyLockerReader;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LockerPinQueryServiceTest {

    @Mock
    private NearbyLockerReader nearbyLockerReader;

    @InjectMocks
    private LockerPinQueryService lockerPinQueryService;

    @Test
    @DisplayName("조회 결과가 없으면 빈 핀 목록을 반환한다")
    void returnsEmptyWhenNoNearbyLockers() {
        given(nearbyLockerReader.findNearby(37.5, 127.0, 500)).willReturn(List.of());

        List<LockerPinResponse> pins = lockerPinQueryService.getPins(37.5, 127.0, 500);

        assertThat(pins).isEmpty();
    }

    @Test
    @DisplayName("클러스터에 1개면 LOCKER, 2개 이상이면 PLACE 핀으로 변환한다")
    void mapsLockerAndPlacePinsByClusterSize() {
        given(nearbyLockerReader.findNearby(37.5, 127.0, 500)).willReturn(List.of(
            new NearbyLocker(1L, "A", "서울 A", 37.5001, 127.0001, 10.0, 10),
            new NearbyLocker(2L, "B", "서울 B", 37.5002, 127.0002, 12.0, 20),
            new NearbyLocker(3L, "C", "서울 C", 37.5004, 127.0004, 14.0, 20)
        ));

        List<LockerPinResponse> pins = lockerPinQueryService.getPins(37.5, 127.0, 500);

        assertThat(pins).hasSize(2);
        assertThat(pins.get(0).pinType()).isEqualTo(LockerPinType.LOCKER);
        assertThat(pins.get(0).lockerId()).isEqualTo(1L);

        assertThat(pins.get(1).pinType()).isEqualTo(LockerPinType.PLACE);
        assertThat(pins.get(1).placeId()).isEqualTo(20L);
        assertThat(pins.get(1).lockerId()).isNull();
    }
}
