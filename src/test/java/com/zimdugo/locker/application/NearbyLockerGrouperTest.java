package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.NearbyLocker;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NearbyLockerGrouperTest {

    private final NearbyLockerGrouper nearbyLockerGrouper = new NearbyLockerGrouper();

    @Test
    @DisplayName("동일 좌표는 하나의 그룹으로 묶는다")
    void groupsBySameCoordinate() {
        List<List<NearbyLocker>> result = nearbyLockerGrouper.groupByCoordinateOrRoadAddress(List.of(
            new NearbyLocker(1L, "강남A", "서울 A", 37.5, 127.0, 10.0),
            new NearbyLocker(2L, "강남B", "서울 B", 37.5, 127.0, 10.0)
        ));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).hasSize(2);
    }

    @Test
    @DisplayName("동일 도로명 주소는 하나의 그룹으로 묶는다")
    void groupsBySameRoadAddress() {
        List<List<NearbyLocker>> result = nearbyLockerGrouper.groupByCoordinateOrRoadAddress(List.of(
            new NearbyLocker(1L, "강남A", "서울 테헤란로 1", 37.5, 127.0, 10.0),
            new NearbyLocker(2L, "강남B", "서울 테헤란로 1", 38.0, 128.0, 10.0)
        ));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).hasSize(2);
    }

    @Test
    @DisplayName("연결된 조건은 하나의 그룹이 된다")
    void groupsTransitively() {
        List<List<NearbyLocker>> result = nearbyLockerGrouper.groupByCoordinateOrRoadAddress(List.of(
            new NearbyLocker(1L, "A", "주소1", 37.5, 127.0, 10.0),
            new NearbyLocker(2L, "B", "주소2", 37.5, 127.0, 11.0),
            new NearbyLocker(3L, "C", "주소2", 38.5, 128.0, 12.0)
        ));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).hasSize(3);
    }

    @Test
    @DisplayName("좌표 미세 오차는 정규화 후 같은 그룹으로 묶는다")
    void groupsByRoundedCoordinate() {
        List<List<NearbyLocker>> result = nearbyLockerGrouper.groupByCoordinateOrRoadAddress(List.of(
            new NearbyLocker(1L, "A", "주소1", 37.50000041, 127.00000041, 10.0),
            new NearbyLocker(2L, "B", "주소2", 37.50000044, 127.00000044, 20.0)
        ));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).hasSize(2);
    }
}
