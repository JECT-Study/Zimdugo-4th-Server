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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockerNearbyQueryServiceTest {

    @Mock
    private NearbyLockerReader nearbyLockerReader;

    @Mock
    private NearbyLockerGrouper nearbyLockerGrouper;

    @Mock
    private NearbyLockerGroupMapper nearbyLockerGroupMapper;

    @InjectMocks
    private LockerNearbyQueryService lockerNearbyQueryService;

    @Test
    @DisplayName("조회 결과가 없으면 빈 리스트를 반환한다")
    void noResultReturnsEmpty() {
        given(nearbyLockerReader.findNearby(37.5, 127.0, 1000)).willReturn(List.of());

        List<NearbyLockerGroupResponse> result = lockerNearbyQueryService.getNearbyLockerGroups(37.5, 127.0, 1000);

        assertThat(result).isEmpty();
        verify(nearbyLockerReader).findNearby(37.5, 127.0, 1000);
    }

    @Test
    @DisplayName("조회 -> 그룹화 -> 응답 매핑 순서로 오케스트레이션 한다")
    void orchestratesReaderGrouperAndMapper() {
        List<NearbyLocker> nearbyLockers = List.of(locker(1L, "강남A", "서울 A", 37.5, 127.0, 20.0));
        List<List<NearbyLocker>> grouped = List.of(nearbyLockers);
        List<NearbyLockerGroupResponse> mapped = List.of(
            NearbyLockerGroupResponse.of(37.5, 127.0, "서울 A", 20L, List.of())
        );

        given(nearbyLockerReader.findNearby(37.5, 127.0, 1000)).willReturn(nearbyLockers);
        given(nearbyLockerGrouper.groupByCoordinateOrRoadAddress(nearbyLockers)).willReturn(grouped);
        given(nearbyLockerGroupMapper.toGroupResponses(grouped)).willReturn(mapped);

        List<NearbyLockerGroupResponse> result = lockerNearbyQueryService.getNearbyLockerGroups(37.5, 127.0, 1000);

        assertThat(result).isEqualTo(mapped);
        verify(nearbyLockerReader).findNearby(37.5, 127.0, 1000);
        verify(nearbyLockerGrouper).groupByCoordinateOrRoadAddress(nearbyLockers);
        verify(nearbyLockerGroupMapper).toGroupResponses(grouped);
    }

    private NearbyLocker locker(
        Long id,
        String name,
        String roadAddress,
        double latitude,
        double longitude,
        double distanceMeters
    ) {
        return new NearbyLocker(id, name, roadAddress, latitude, longitude, distanceMeters);
    }
}
