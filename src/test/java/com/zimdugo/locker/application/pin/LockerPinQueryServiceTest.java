package com.zimdugo.locker.application.pin;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.search.LockerSearchResultQueryService;
import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.application.result.search.LockerSearchItemResult;
import com.zimdugo.locker.application.result.pin.LockerPinItemResult;
import com.zimdugo.locker.application.result.pin.LockerPinResult;
import com.zimdugo.locker.domain.favorite.FavoriteLockerReader;
import com.zimdugo.locker.domain.locker.NearbyLocker;
import com.zimdugo.locker.domain.locker.NearbyLockerPlaceReader;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockerPinQueryServiceTest {

    private static final LockerPinQuery DETAIL_QUERY = new LockerPinQuery(
        37.54,
        126.92,
        37.56,
        126.94,
        13.0
    );

    @Mock
    private NearbyLockerPlaceReader nearbyLockerPlaceReader;

    @Mock
    private LockerPinAssembler lockerPinAssembler;

    @Mock
    private LockerSearchPinAssembler lockerSearchPinAssembler;

    @Mock
    private LockerPinClusterer lockerPinClusterer;

    @Mock
    private FavoriteLockerReader favoriteLockerReader;

    @Mock
    private LockerSearchResultQueryService lockerSearchResultQueryService;

    @InjectMocks
    private LockerPinQueryService lockerPinQueryService;

    @Test
    @DisplayName("주변 보관함이 없으면 빈 결과를 반환한다")
    void returnsEmptyWhenNoNearbyLockers() {
        given(nearbyLockerPlaceReader.findWithinBounds(
            37.54,
            126.92,
            37.56,
            126.94,
            LockerSearchFilter.empty()
        )).willReturn(List.of());

        LockerPinResult result = lockerPinQueryService.getPins(1L, DETAIL_QUERY);

        assertThat(result.count()).isZero();
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("주변 보관함이 있으면 assembler 결과를 반환한다")
    void returnsAssemblerResultWhenNearbyLockersExist() {
        List<NearbyLocker> nearbyLockers = List.of(sampleLocker());
        List<LockerPinItemResult> pins = List.of(
            LockerPinItemResult.locker(1L, 37.55, 126.93, true)
        );

        given(nearbyLockerPlaceReader.findWithinBounds(
            37.54,
            126.92,
            37.56,
            126.94,
            LockerSearchFilter.empty()
        )).willReturn(nearbyLockers);
        given(favoriteLockerReader.findFavoriteLockerIds(1L, Set.of(1L))).willReturn(Set.of(1L));
        given(lockerPinAssembler.assemble(nearbyLockers, Set.of(1L))).willReturn(pins);
        given(lockerPinClusterer.cluster(pins, 13.0)).willReturn(pins);

        LockerPinResult result = lockerPinQueryService.getPins(1L, DETAIL_QUERY);

        assertThat(result.count()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        verify(lockerPinAssembler).assemble(nearbyLockers, Set.of(1L));
        verify(lockerPinClusterer).cluster(pins, 13.0);
    }

    @Test
    @DisplayName("지도 경계 좌표가 범위를 벗어나면 보관함을 조회하지 않는다")
    void doesNotReadNearbyLockersWhenBoundsLocationIsOutOfRange() {
        LockerPinQuery query = new LockerPinQuery(90.1, 126.92, 37.56, 126.94, 13.0);

        assertThatThrownBy(() -> lockerPinQueryService.getPins(1L, query))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_LOCATION_RANGE);

        verify(nearbyLockerPlaceReader, never()).findWithinBounds(
            90.1,
            126.92,
            37.56,
            126.94,
            LockerSearchFilter.empty()
        );
    }

    @Test
    @DisplayName("남서쪽 좌표가 북동쪽 좌표보다 크면 보관함을 조회하지 않는다")
    void doesNotReadNearbyLockersWhenBoundsOrderIsInvalid() {
        LockerPinQuery query = new LockerPinQuery(37.57, 126.92, 37.56, 126.94, 13.0);

        assertThatThrownBy(() -> lockerPinQueryService.getPins(1L, query))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_LOCATION_RANGE);

        verify(nearbyLockerPlaceReader, never()).findWithinBounds(
            37.57,
            126.92,
            37.56,
            126.94,
            LockerSearchFilter.empty()
        );
    }

    @Test
    @DisplayName("키워드 검색 핀은 검색 결과를 bounds 안에서만 클러스터링한다")
    void clustersKeywordPinsWithinBounds() {
        LockerPinQuery query = new LockerPinQuery(
            37.54,
            126.92,
            37.56,
            126.94,
            13.0,
            37.55,
            126.93,
            "station",
            Set.of("SMALL"),
            Set.of("INDOOR"),
            Set.of("SUBWAY_STATION")
        );
        List<LockerSearchItemResult> items = List.of(sampleKeywordLockerItem());
        List<LockerPinItemResult> assembledPins = List.of(
            LockerPinItemResult.locker(1L, 37.55, 126.93, true),
            LockerPinItemResult.locker(2L, 37.7, 127.1, false)
        );
        List<LockerPinItemResult> clusteredPins = List.of(
            LockerPinItemResult.locker(1L, 37.55, 126.93, true)
        );

        given(lockerSearchResultQueryService.getDisplayableSearchItemsForPins(
            1L,
            37.55,
            126.93,
            "station",
            LockerSearchFilter.from(Set.of("SMALL"), Set.of("INDOOR"), Set.of("SUBWAY_STATION"))
        )).willReturn(items);
        given(lockerSearchPinAssembler.assemble(items)).willReturn(assembledPins);
        given(lockerPinClusterer.cluster(List.of(assembledPins.get(0)), 13.0)).willReturn(clusteredPins);

        LockerPinResult result = lockerPinQueryService.getPins(1L, query);

        assertThat(result.count()).isEqualTo(1);
        assertThat(result.items()).containsExactlyElementsOf(clusteredPins);
        verify(nearbyLockerPlaceReader, never()).findWithinBounds(
            37.54,
            126.92,
            37.56,
            126.94,
            LockerSearchFilter.from(Set.of("SMALL"), Set.of("INDOOR"), Set.of("SUBWAY_STATION"))
        );
        verify(lockerSearchPinAssembler).assemble(items);
        verify(lockerPinClusterer).cluster(List.of(assembledPins.get(0)), 13.0);
    }

    @Test
    @DisplayName("일반 bounds 핀 조회에도 필터를 적용한다")
    void appliesFilterToBoundsPins() {
        LockerPinQuery query = new LockerPinQuery(
            37.54,
            126.92,
            37.56,
            126.94,
            13.0,
            null,
            null,
            null,
            Set.of("LARGE"),
            Set.of("INDOOR"),
            Set.of("SUBWAY_STATION")
        );
        List<NearbyLocker> nearbyLockers = List.of(sampleLocker());
        List<LockerPinItemResult> pins = List.of(
            LockerPinItemResult.locker(1L, 37.55, 126.93, false)
        );
        LockerSearchFilter filter = LockerSearchFilter.from(
            Set.of("LARGE"),
            Set.of("INDOOR"),
            Set.of("SUBWAY_STATION")
        );

        given(nearbyLockerPlaceReader.findWithinBounds(37.54, 126.92, 37.56, 126.94, filter))
            .willReturn(nearbyLockers);
        given(lockerPinAssembler.assemble(nearbyLockers, Set.of())).willReturn(pins);
        given(lockerPinClusterer.cluster(pins, 13.0)).willReturn(pins);

        LockerPinResult result = lockerPinQueryService.getPins(null, query);

        assertThat(result.items()).containsExactlyElementsOf(pins);
        verify(nearbyLockerPlaceReader).findWithinBounds(37.54, 126.92, 37.56, 126.94, filter);
    }

    private NearbyLocker sampleLocker() {
        return new NearbyLocker(
            1L,
            37.55,
            126.93,
            1L
        );
    }

    private LockerSearchItemResult sampleKeywordLockerItem() {
        return new LockerSearchItemResult(
            LockerItemType.LOCKER,
            1L,
            "Place",
            1L,
            "Locker",
            "Road",
            "NORMAL",
            1000,
            37.55,
            126.93,
            10L,
            LocalDateTime.of(2026, 6, 28, 12, 0),
            true,
            List.of()
        );
    }
}
