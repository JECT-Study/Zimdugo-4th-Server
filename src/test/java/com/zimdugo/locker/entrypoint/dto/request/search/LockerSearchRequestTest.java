package com.zimdugo.locker.entrypoint.dto.request.search;

import com.zimdugo.locker.application.search.LockerSearchCommand;
import com.zimdugo.locker.application.filter.IndoorOutdoorFilterType;
import com.zimdugo.locker.application.filter.LockerFacilityFilterType;
import com.zimdugo.locker.application.filter.LockerSizeFilterType;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockerSearchRequestTest {

    @Test
    @DisplayName("검색 요청은 enum 필터를 유지한 command로 변환한다")
    void toCommandPreservesTypedFilters() {
        LockerSearchRequest request = new LockerSearchRequest(
            37.55,
            126.93,
            "신촌",
            Set.of(LockerSizeFilterType.SMALL, LockerSizeFilterType.LARGE),
            Set.of(IndoorOutdoorFilterType.INDOOR),
            Set.of(LockerFacilityFilterType.SUBWAY_STATION)
        );

        LockerSearchCommand command = request.toCommand();

        assertThat(command.sizeTypes()).containsExactlyInAnyOrder(
            LockerSizeFilterType.SMALL,
            LockerSizeFilterType.LARGE
        );
        assertThat(command.indoorOutdoorTypes()).containsExactly(IndoorOutdoorFilterType.INDOOR);
        assertThat(command.lockerTypes()).containsExactly(LockerFacilityFilterType.SUBWAY_STATION);
    }
}
