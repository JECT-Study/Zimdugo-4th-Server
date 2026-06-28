package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.locker.NearbyLocker;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.projection.NearbyLockerPlaceQueryProjection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NearbyLockerPlaceReaderAdapterTest {

    @Mock
    private LockerRepository lockerRepository;

    @InjectMocks
    private NearbyLockerPlaceReaderAdapter nearbyLockerPlaceReaderAdapter;

    @Test
    @DisplayName("bounds 핀 조회에도 복합 필터를 적용한다")
    void filtersNearbyLockersWithinBounds() {
        given(lockerRepository.findLockersWithinBounds(37.54, 126.92, 37.56, 126.94))
            .willReturn(List.of(
                projection(1L, 101L, "SMALL,LARGE", "INDOOR", "SUBWAY_STATION"),
                projection(2L, 101L, "MEDIUM", "INDOOR", "SUBWAY_STATION"),
                projection(3L, 102L, "LARGE", "OUTDOOR", "SUBWAY_STATION")
            ));

        LockerSearchFilter filter = new LockerSearchFilter(
            Set.of(LockerSizeType.LARGE),
            Set.of(IndoorOutdoorType.INDOOR),
            Set.of(LockerType.SUBWAY_STATION)
        );

        List<NearbyLocker> result = nearbyLockerPlaceReaderAdapter.findWithinBounds(
            37.54,
            126.92,
            37.56,
            126.94,
            filter
        );

        assertThat(result).extracting(NearbyLocker::id).containsExactly(1L);
        assertThat(result.getFirst().placeId()).isEqualTo(101L);
    }

    private NearbyLockerPlaceQueryProjection projection(
        Long lockerId,
        Long placeId,
        String lockerSize,
        String indoorOutdoorType,
        String lockerType
    ) {
        return new NearbyLockerPlaceQueryProjection() {
            @Override
            public Long getLockerId() {
                return lockerId;
            }

            @Override
            public double getLockerLatitude() {
                return 37.55;
            }

            @Override
            public double getLockerLongitude() {
                return 126.93;
            }

            @Override
            public Long getPlaceId() {
                return placeId;
            }

            @Override
            public String getLockerType() {
                return lockerType;
            }

            @Override
            public String getIndoorOutdoorType() {
                return indoorOutdoorType;
            }

            @Override
            public String getLockerSize() {
                return lockerSize;
            }
        };
    }
}
