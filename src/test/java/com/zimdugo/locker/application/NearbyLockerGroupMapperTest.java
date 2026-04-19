package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.NearbyLocker;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NearbyLockerGroupMapperTest {

    private final NearbyLockerGroupMapper nearbyLockerGroupMapper = new NearbyLockerGroupMapper();

    @Test
    @DisplayName("그룹 내부 보관함과 그룹 리스트를 모두 거리 오름차순으로 정렬한다")
    void sortsInsideAndOutsideByDistance() {
        List<List<NearbyLocker>> grouped = List.of(
            List.of(
                locker(1L, "A", "주소A", 37.5, 127.0, 300.0),
                locker(2L, "B", "주소A", 37.5, 127.0, 100.0)
            ),
            List.of(
                locker(3L, "C", "주소C", 38.5, 128.0, 200.0)
            )
        );

        List<NearbyLockerGroupResponse> result = nearbyLockerGroupMapper.toGroupResponses(grouped);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).distanceMeters()).isEqualTo(100L);
        assertThat(result.get(0).lockers().get(0).distanceMeters()).isEqualTo(100L);
        assertThat(result.get(0).lockers().get(1).distanceMeters()).isEqualTo(300L);
        assertThat(result.get(1).distanceMeters()).isEqualTo(200L);
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
