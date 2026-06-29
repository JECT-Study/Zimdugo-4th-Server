package com.zimdugo.locker.domain.search;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LockerSearchFilterTest {

    @Test
    @DisplayName("사이즈 필터는 복수 값 중 하나가 일치하면 통과한다")
    void matchesAnySelectedSize() {
        LockerSearchFilter filter = new LockerSearchFilter(
            Set.of(LockerSizeType.SMALL, LockerSizeType.LARGE),
            Set.of(),
            Set.of()
        );

        assertThat(filter.matches(Set.of(LockerSizeType.SMALL), IndoorOutdoorType.OUTDOOR, LockerType.ETC)).isTrue();
        assertThat(filter.matches(Set.of(LockerSizeType.LARGE), IndoorOutdoorType.INDOOR, LockerType.SUBWAY_STATION))
            .isTrue();
        assertThat(filter.matches(
            Set.of(LockerSizeType.MEDIUM),
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
            Set.of(LockerSizeType.SMALL, LockerSizeType.LARGE),
            IndoorOutdoorType.INDOOR,
            LockerType.SUBWAY_STATION
        )).isTrue();
        assertThat(filter.matches(
            Set.of(LockerSizeType.LARGE),
            IndoorOutdoorType.OUTDOOR,
            LockerType.SUBWAY_STATION
        )).isFalse();
    }

    @Test
    @DisplayName("생성자는 null 필터를 빈 집합으로 정규화한다")
    void normalizesNullFiltersToEmptySets() {
        LockerSearchFilter filter = new LockerSearchFilter(null, null, null);

        assertThat(filter.sizeTypes()).isEmpty();
        assertThat(filter.indoorOutdoorTypes()).isEmpty();
        assertThat(filter.lockerTypes()).isEmpty();
        assertThat(filter.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("생성된 필터 집합은 외부에서 수정할 수 없다")
    void exposesUnmodifiableSets() {
        LockerSearchFilter filter = new LockerSearchFilter(
            Set.of(LockerSizeType.SMALL),
            Set.of(IndoorOutdoorType.INDOOR),
            Set.of(LockerType.ETC)
        );

        assertThatThrownBy(() -> filter.sizeTypes().add(LockerSizeType.LARGE))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("실내외 필터는 복수 값 중 하나가 일치하면 통과한다")
    void matchesAnySelectedIndoorOutdoorType() {
        LockerSearchFilter filter = new LockerSearchFilter(
            Set.of(),
            Set.of(IndoorOutdoorType.INDOOR, IndoorOutdoorType.OUTDOOR),
            Set.of()
        );

        assertThat(filter.matches(Set.of(LockerSizeType.SMALL), IndoorOutdoorType.INDOOR, LockerType.ETC)).isTrue();
        assertThat(filter.matches(Set.of(LockerSizeType.SMALL), IndoorOutdoorType.OUTDOOR, LockerType.ETC)).isTrue();
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
            Set.of(LockerSizeType.SMALL),
            IndoorOutdoorType.INDOOR,
            LockerType.SUBWAY_STATION
        )).isTrue();
        assertThat(filter.matches(
            Set.of(LockerSizeType.SMALL),
            IndoorOutdoorType.INDOOR,
            LockerType.DEPARTMENT_STORE
        )).isTrue();
        assertThat(filter.matches(Set.of(LockerSizeType.SMALL), IndoorOutdoorType.INDOOR, LockerType.ETC)).isFalse();
    }
}
