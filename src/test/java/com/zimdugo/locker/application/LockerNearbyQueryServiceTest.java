package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.NearbyLocker;
import com.zimdugo.locker.domain.NearbyLockerReader;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockerNearbyQueryServiceTest {

    @Mock
    private NearbyLockerReader nearbyLockerReader;

    @InjectMocks
    private LockerNearbyQueryService lockerNearbyQueryService;

    @Nested
    @DisplayName("가까운 보관함 조회")
    class GetNearbyLockers {

        @Test
        @DisplayName("조회 결과를 response dto로 변환한다")
        void resultToResponse() {
            given(nearbyLockerReader.findNearby(37.5, 127.0, 1000))
                .willReturn(List.of(
                    new NearbyLocker(
                        1L,
                        "강남역 보관함",
                        "서울특별시 강남구 테헤란로 123",
                        37.4983,
                        127.0272,
                        53.4
                    )
                ));

            List<NearbyLockerResponse> result = lockerNearbyQueryService.getNearbyLockers(37.5, 127.0, 1000);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().id()).isEqualTo(1L);
            assertThat(result.getFirst().name()).isEqualTo("강남역 보관함");
            assertThat(result.getFirst().distanceMeters()).isEqualTo(53L);
            verify(nearbyLockerReader).findNearby(37.5, 127.0, 1000);
        }

        @Test
        @DisplayName("조회 결과가 없으면 빈 리스트를 반환한다")
        void noResultEmptyList() {
            given(nearbyLockerReader.findNearby(37.5, 127.0, 1000))
                .willReturn(List.of());

            List<NearbyLockerResponse> result = lockerNearbyQueryService.getNearbyLockers(37.5, 127.0, 1000);

            assertThat(result).isEmpty();
            verify(nearbyLockerReader).findNearby(37.5, 127.0, 1000);
        }
    }
}
