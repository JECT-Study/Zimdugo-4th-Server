package com.zimdugo.locker.application.result;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeoBoundsUtilsTest {

    @Test
    @DisplayName("좌표 목록의 남서/북동 bounds를 계산한다")
    void calculatesBoundsFromCoordinates() {
        List<TestCoordinate> coordinates = List.of(
            new TestCoordinate(37.557, 126.924),
            new TestCoordinate(37.551, 126.936),
            new TestCoordinate(37.559, 126.921)
        );

        Optional<LockerBoundsResult> bounds = GeoBoundsUtils.from(
            coordinates,
            TestCoordinate::latitude,
            TestCoordinate::longitude
        );

        assertThat(bounds).isPresent();
        assertThat(bounds.get().swLat()).isEqualTo(37.551);
        assertThat(bounds.get().swLng()).isEqualTo(126.921);
        assertThat(bounds.get().neLat()).isEqualTo(37.559);
        assertThat(bounds.get().neLng()).isEqualTo(126.936);
    }

    @Test
    @DisplayName("빈 좌표 목록이면 bounds를 만들지 않는다")
    void returnsEmptyWhenCoordinatesAreEmpty() {
        Optional<LockerBoundsResult> bounds = GeoBoundsUtils.from(
            List.<TestCoordinate>of(),
            TestCoordinate::latitude,
            TestCoordinate::longitude
        );

        assertThat(bounds).isEmpty();
    }

    private record TestCoordinate(
        double latitude,
        double longitude
    ) {
    }
}
