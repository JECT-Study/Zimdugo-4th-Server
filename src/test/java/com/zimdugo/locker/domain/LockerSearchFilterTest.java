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
            Set.of(LockerSizeType.SMALL, LockerSizeType.LARGE),
            Set.of(),
            Set.of()
        );

        assertThat(filter.matches(LockerSizeType.SMALL, IndoorOutdoorType.OUTDOOR, LockerType.ETC)).isTrue();
        assertThat(filter.matches(LockerSizeType.LARGE, IndoorOutdoorType.INDOOR, LockerType.SUBWAY_STATION)).isTrue();
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
            Set.of(LockerSizeType.LARGE),
            Set.of(IndoorOutdoorType.INDOOR),
            Set.of(LockerType.SUBWAY_STATION)
        );

        assertThat(filter.matches(
            LockerSizeType.LARGE,
            IndoorOutdoorType.INDOOR,
            LockerType.SUBWAY_STATION
        )).isTrue();
        assertThat(filter.matches(
            LockerSizeType.LARGE,
            IndoorOutdoorType.OUTDOOR,
            LockerType.SUBWAY_STATION
        )).isFalse();
    }

    @Test
    @DisplayName("API 문자열 필터를 도메인 enum으로 변환한다")
    void parsesApiFilterValues() {
        LockerSearchFilter filter = LockerSearchFilter.from(
            Set.of("SMALL,LARGE"),
            Set.of("indoor"),
            Set.of("subway_station")
        );

        assertThat(filter.sizeTypes()).containsExactlyInAnyOrder(LockerSizeType.SMALL, LockerSizeType.LARGE);
        assertThat(filter.indoorOutdoorTypes()).containsExactly(IndoorOutdoorType.INDOOR);
        assertThat(filter.lockerTypes()).containsExactly(LockerType.SUBWAY_STATION);
    }

    @Test
    @DisplayName("실내외 필터는 복수 값 중 하나가 일치하면 통과한다")
    void matchesAnySelectedIndoorOutdoorType() {
        LockerSearchFilter filter = new LockerSearchFilter(
            Set.of(),
            Set.of(IndoorOutdoorType.INDOOR, IndoorOutdoorType.OUTDOOR),
            Set.of()
        );

        assertThat(filter.matches(LockerSizeType.SMALL, IndoorOutdoorType.INDOOR, LockerType.ETC)).isTrue();
        assertThat(filter.matches(LockerSizeType.SMALL, IndoorOutdoorType.OUTDOOR, LockerType.ETC)).isTrue();
    }

    @Test
    @DisplayName("보관함 유형 필터는 복수 값 중 하나가 일치하면 통과한다")
    void matchesAnySelectedLockerType() {
        LockerSearchFilter filter = new LockerSearchFilter(
            Set.of(),
            Set.of(),
            Set.of(LockerType.SUBWAY_STATION, LockerType.DEPARTMENT_STORE)
        );

        assertThat(filter.matches(
            LockerSizeType.SMALL,
            IndoorOutdoorType.INDOOR,
            LockerType.SUBWAY_STATION
        )).isTrue();
        assertThat(filter.matches(
            LockerSizeType.SMALL,
            IndoorOutdoorType.INDOOR,
            LockerType.DEPARTMENT_STORE
        )).isTrue();
        assertThat(filter.matches(LockerSizeType.SMALL, IndoorOutdoorType.INDOOR, LockerType.ETC)).isFalse();
    }
}
