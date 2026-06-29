package com.zimdugo.locker.application.place;

import com.zimdugo.common.i18n.CurrentRequestLanguage;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.result.place.PlaceLockerResult;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.place.LockerPlace;
import com.zimdugo.locker.domain.place.LockerPlaceLocker;
import com.zimdugo.locker.domain.place.LockerPlaceLockerReader;
import com.zimdugo.locker.domain.place.LockerPlaceReader;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PlaceLockerQueryServiceTest {

    @Mock
    private LockerPlaceReader lockerPlaceReader;

    @Mock
    private LockerPlaceLockerReader lockerPlaceLockerReader;

    @Mock
    private CurrentRequestLanguage currentRequestLanguage;

    @InjectMocks
    private PlaceLockerQueryService placeLockerQueryService;

    @BeforeEach
    void setUp() {
        given(currentRequestLanguage.resolve()).willReturn(SupportedLanguage.KOREAN);
    }

    @Test
    @DisplayName("장소 ID와 keyword 검색 필터로 하위 보관함을 조회한다")
    void returnsPlaceLockersWithKeywordFilter() {
        PlaceLockerQueryCommand command = command();
        LockerSearchFilter filter = new LockerSearchFilter(
            Set.of(LockerSizeType.LARGE),
            Set.of(IndoorOutdoorType.INDOOR),
            Set.of(LockerType.SUBWAY_STATION)
        );
        given(lockerPlaceReader.readById(101L, "ko")).willReturn(Optional.of(place()));
        given(lockerPlaceLockerReader.readByPlaceIds(37.55, 126.93, List.of(101L), filter, "ko"))
            .willReturn(Map.of(101L, List.of(locker())));

        PlaceLockerResult result = placeLockerQueryService.getPlaceLockers(command);

        assertThat(result.placeId()).isEqualTo(101L);
        assertThat(result.placeName()).isEqualTo("신촌역");
        assertThat(result.bounds().swLat()).isEqualTo(37.556);
        assertThat(result.bounds().swLng()).isEqualTo(126.923);
        assertThat(result.bounds().neLat()).isEqualTo(37.556);
        assertThat(result.bounds().neLng()).isEqualTo(126.923);
        assertThat(result.lockers()).extracting(locker -> locker.lockerId()).containsExactly(10L);
        then(lockerPlaceLockerReader).should().readByPlaceIds(37.55, 126.93, List.of(101L), filter, "ko");
    }

    @Test
    @DisplayName("필터에 맞는 하위 보관함이 없어도 장소와 빈 목록을 반환한다")
    void returnsPlaceWithEmptyLockers() {
        given(lockerPlaceReader.readById(101L, "ko")).willReturn(Optional.of(place()));
        given(lockerPlaceLockerReader.readByPlaceIds(
            37.55,
            126.93,
            List.of(101L),
            new LockerSearchFilter(
                Set.of(LockerSizeType.LARGE),
                Set.of(IndoorOutdoorType.INDOOR),
                Set.of(LockerType.SUBWAY_STATION)
            ),
            "ko"
        )).willReturn(Map.of());

        PlaceLockerResult result = placeLockerQueryService.getPlaceLockers(command());

        assertThat(result.placeId()).isEqualTo(101L);
        assertThat(result.bounds()).isNull();
        assertThat(result.lockers()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 장소이면 404 예외를 발생시킨다")
    void throwsWhenPlaceDoesNotExist() {
        given(lockerPlaceReader.readById(101L, "ko")).willReturn(Optional.empty());

        assertThatThrownBy(() -> placeLockerQueryService.getPlaceLockers(command()))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PLACE_NOT_FOUND);

        then(lockerPlaceLockerReader).shouldHaveNoInteractions();
    }

    private PlaceLockerQueryCommand command() {
        return new PlaceLockerQueryCommand(
            101L,
            37.55,
            126.93,
            Set.of("LARGE"),
            Set.of("INDOOR"),
            Set.of("SUBWAY_STATION")
        );
    }

    private LockerPlace place() {
        return new LockerPlace(101L, "신촌역", "서울 서대문구", 37.557, 126.924);
    }

    private LockerPlaceLocker locker() {
        return new LockerPlaceLocker(
            101L,
            10L,
            "신촌역 보관함",
            "서울 서대문구",
            LockerType.SUBWAY_STATION,
            IndoorOutdoorType.INDOOR,
            Set.of(LockerSizeType.LARGE),
            1000,
            37.556,
            126.923,
            95L,
            LocalDateTime.of(2026, 6, 7, 12, 0)
        );
    }
}
