package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.IndoorOutdoorType;
import com.zimdugo.locker.domain.LockerPlaceLocker;
import com.zimdugo.locker.domain.LockerSearchFilter;
import com.zimdugo.locker.domain.LockerSizeType;
import com.zimdugo.locker.domain.LockerType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LockerPlaceLockerReaderAdapterTest {

    @Mock
    private LockerRepository lockerRepository;

    @InjectMocks
    private LockerPlaceLockerReaderAdapter lockerPlaceLockerReaderAdapter;

    @Test
    @DisplayName("PLACE 하위 보관함에도 복합 필터를 적용한다")
    void filtersPlaceLockers() {
        List<LockerPlaceLockerQueryProjection> projections = List.of(
            projection(10L, "SMALL,LARGE", "INDOOR", "SUBWAY_STATION"),
            projection(11L, "MEDIUM", "INDOOR", "SUBWAY_STATION"),
            projection(12L, "LARGE", "OUTDOOR", "SUBWAY_STATION")
        );
        given(lockerRepository.findByPlaceIds(37.55, 126.93, List.of(101L)))
            .willReturn(projections);
        LockerSearchFilter filter = new LockerSearchFilter(
            Set.of(LockerSizeType.LARGE),
            Set.of(IndoorOutdoorType.INDOOR),
            Set.of(LockerType.SUBWAY_STATION)
        );

        Map<Long, List<LockerPlaceLocker>> result = lockerPlaceLockerReaderAdapter.readByPlaceIds(
            37.55,
            126.93,
            List.of(101L),
            filter
        );

        assertThat(result.get(101L)).extracting(LockerPlaceLocker::lockerId).containsExactly(10L);
    }

    private LockerPlaceLockerQueryProjection projection(
        Long lockerId,
        String lockerSize,
        String indoorOutdoorType,
        String lockerType
    ) {
        LockerPlaceLockerQueryProjection projection = Mockito.mock(
            LockerPlaceLockerQueryProjection.class,
            Mockito.withSettings().lenient()
        );
        given(projection.getPlaceId()).willReturn(101L);
        given(projection.getLockerId()).willReturn(lockerId);
        given(projection.getLockerName()).willReturn("보관함");
        given(projection.getRoadAddress()).willReturn("서울");
        given(projection.getLockerType()).willReturn(lockerType);
        given(projection.getIndoorOutdoorType()).willReturn(indoorOutdoorType);
        given(projection.getLockerSize()).willReturn(lockerSize);
        given(projection.getMinPrice()).willReturn(1000);
        given(projection.getLockerLatitude()).willReturn(37.55);
        given(projection.getLockerLongitude()).willReturn(126.93);
        given(projection.getDistanceMeters()).willReturn(100.0);
        given(projection.getUpdatedAt()).willReturn(LocalDateTime.of(2026, 6, 7, 12, 0));
        return projection;
    }
}
