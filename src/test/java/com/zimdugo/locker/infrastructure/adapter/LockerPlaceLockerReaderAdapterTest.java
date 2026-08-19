package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.place.LockerPlaceLocker;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.projection.LockerPlaceLockerQueryProjection;
import com.zimdugo.locker.infrastructure.projection.LockerSizeTypeQueryProjection;
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
            projection(10L, "Translated locker 10", "INDOOR", "SUBWAY_STATION"),
            projection(11L, "Translated locker 11", "INDOOR", "SUBWAY_STATION"),
            projection(12L, "Translated locker 12", "OUTDOOR", "SUBWAY_STATION")
        );
        given(lockerRepository.findByPlaceIds(37.55, 126.93, List.of(101L), "ko"))
            .willReturn(projections);
        LockerSizeTypeQueryProjection small = sizeType(10L, "SMALL");
        LockerSizeTypeQueryProjection large = sizeType(10L, "LARGE");
        LockerSizeTypeQueryProjection medium = sizeType(11L, "MEDIUM");
        LockerSizeTypeQueryProjection outdoorLarge = sizeType(12L, "LARGE");
        given(lockerRepository.findLockerSizeTypesByLockerIds(List.of(10L, 11L, 12L)))
            .willReturn(List.of(small, large, medium, outdoorLarge));

        LockerSearchFilter filter = new LockerSearchFilter(
            Set.of(LockerSizeType.LARGE),
            Set.of(IndoorOutdoorType.INDOOR),
            Set.of(LockerType.SUBWAY_STATION)
        );

        Map<Long, List<LockerPlaceLocker>> result = lockerPlaceLockerReaderAdapter.readByPlaceIds(
            37.55,
            126.93,
            List.of(101L),
            filter,
            "ko"
        );

        assertThat(result.get(101L)).extracting(LockerPlaceLocker::lockerId).containsExactly(10L);
        assertThat(result.get(101L).getFirst().lockerName()).isEqualTo("Translated locker 10");
        assertThat(result.get(101L).getFirst().lockerSizes())
            .containsExactlyInAnyOrder(LockerSizeType.SMALL, LockerSizeType.LARGE);
    }

    private LockerPlaceLockerQueryProjection projection(
        Long lockerId,
        String lockerName,
        String indoorOutdoorType,
        String lockerType
    ) {
        LockerPlaceLockerQueryProjection projection = Mockito.mock(
            LockerPlaceLockerQueryProjection.class,
            Mockito.withSettings().lenient()
        );
        given(projection.getPlaceId()).willReturn(101L);
        given(projection.getLockerId()).willReturn(lockerId);
        given(projection.getLockerName()).willReturn(lockerName);
        given(projection.getRoadAddress()).willReturn("Translated address");
        given(projection.getLockerType()).willReturn(lockerType);
        given(projection.getIndoorOutdoorType()).willReturn(indoorOutdoorType);
        given(projection.getMinPrice()).willReturn(1000);
        given(projection.getLockerLatitude()).willReturn(37.55);
        given(projection.getLockerLongitude()).willReturn(126.93);
        given(projection.getDistanceMeters()).willReturn(100.0);
        given(projection.getUpdatedAt()).willReturn(LocalDateTime.of(2026, 6, 7, 12, 0));
        return projection;
    }

    private LockerSizeTypeQueryProjection sizeType(Long lockerId, String sizeType) {
        LockerSizeTypeQueryProjection projection = Mockito.mock(LockerSizeTypeQueryProjection.class);
        given(projection.getLockerId()).willReturn(lockerId);
        given(projection.getSizeType()).willReturn(sizeType);
        return projection;
    }
}
