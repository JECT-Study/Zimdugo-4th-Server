package com.zimdugo.locker.domain;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockerSearchFilterTest {

    @Test
    @DisplayName("사이즈 필터는 복수 값 중 하나가 일치하면 통과한다")
    void matchesAnySelectedSize() {
        LockerSearchFilter filter = new LockerSearchFilter(
            Set.of(LockerSizeType.SMALL, LockerSizeType.BIG),
            null,
            null
        );

        assertThat(filter.matches(LockerSizeType.SMALL, IndoorOutdoorType.OUTDOOR, LockerType.ETC)).isTrue();
        assertThat(filter.matches(LockerSizeType.BIG, IndoorOutdoorType.INDOOR, LockerType.SUBWAY_STATION)).isTrue();
        assertThat(filter.matches(
            LockerSizeType.MEDIUM,
            IndoorOutdoorType.INDOOR,
            LockerType.SUBWAY_STATION
        )).isFalse();
    }

    @Test
    @DisplayName("서로 다른 필터 종류는 모두 일치해야 통과한다")
    void matchesAllFilterTypes() {
        LockerSearchFilter filter = new LockerSearchFilter(
            Set.of(LockerSizeType.BIG),
            IndoorOutdoorType.INDOOR,
            LockerType.SUBWAY_STATION
        );

        assertThat(filter.matches(
            LockerSizeType.BIG,
            IndoorOutdoorType.INDOOR,
            LockerType.SUBWAY_STATION
        )).isTrue();
        assertThat(filter.matches(
            LockerSizeType.BIG,
            IndoorOutdoorType.OUTDOOR,
            LockerType.SUBWAY_STATION
        )).isFalse();
    }

    @Test
    @DisplayName("API 문자열 필터를 도메인 enum으로 변환한다")
    void parsesApiFilterValues() {
        LockerSearchFilter filter = LockerSearchFilter.from(
            Set.of("SMALL,BIG"),
            "indoor",
            "subway_station"
        );

        assertThat(filter.sizeTypes()).containsExactlyInAnyOrder(LockerSizeType.SMALL, LockerSizeType.BIG);
        assertThat(filter.indoorOutdoorType()).isEqualTo(IndoorOutdoorType.INDOOR);
        assertThat(filter.lockerType()).isEqualTo(LockerType.SUBWAY_STATION);
    }
}
