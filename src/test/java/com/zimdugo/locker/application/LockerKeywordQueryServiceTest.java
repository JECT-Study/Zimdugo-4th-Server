package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.keyword.LockerKeywordItemResult;
import com.zimdugo.locker.application.result.keyword.LockerKeywordResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestType;
import com.zimdugo.locker.domain.LockerPlaceLocker;
import com.zimdugo.locker.domain.LockerPlaceLockerReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LockerKeywordQueryServiceTest {

    @Mock
    private LockerSearchQueryService lockerSearchQueryService;

    @Mock
    private LockerPlaceLockerReader lockerPlaceLockerReader;

    @InjectMocks
    private LockerKeywordQueryService lockerKeywordQueryService;

    @Test
    @DisplayName("suggest 결과가 비어있으면 빈 keyword 결과를 반환한다")
    void returnsEmptyWhenSuggestResultIsEmpty() {
        given(lockerSearchQueryService.search(37.55, 126.93, "신촌", 10))
            .willReturn(List.of());

        LockerKeywordResult result = lockerKeywordQueryService.getKeywordResults(37.55, 126.93, "신촌", 10);

        assertThat(result.count()).isZero();
        assertThat(result.bounds()).isNull();
        assertThat(result.items()).isEmpty();
        then(lockerPlaceLockerReader).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("PLACE 결과는 하위 보관함 목록을 포함한다")
    void includesPlaceChildrenWhenResultIsPlace() {
        LockerSuggestItemResult placeItem = new LockerSuggestItemResult(
            LockerSuggestType.PLACE,
            101L,
            "신촌역 1번 출구",
            null,
            null,
            "서울 서대문구 신촌역로 1",
            null,
            37.557,
            126.924,
            100L,
            null
        );
        given(lockerSearchQueryService.search(37.55, 126.93, "신촌", 10))
            .willReturn(List.of(placeItem));
        given(lockerPlaceLockerReader.readByPlaceIds(37.55, 126.93, List.of(101L)))
            .willReturn(Map.of(
                101L,
                List.of(new LockerPlaceLocker(
                    101L,
                    10L,
                    "신촌역 1번 출구 b1 관리사무소 옆",
                    "서울 서대문구 신촌역로 1",
                    "SUBWAY_STATION",
                    37.556,
                    126.923,
                    95L,
                    LocalDateTime.of(2026, 5, 31, 12, 0)
                ))
            ));

        LockerKeywordResult result = lockerKeywordQueryService.getKeywordResults(37.55, 126.93, "신촌", 10);

        assertThat(result.count()).isEqualTo(1);
        assertThat(result.bounds().swLat()).isEqualTo(37.557);
        assertThat(result.bounds().swLng()).isEqualTo(126.924);
        assertThat(result.bounds().neLat()).isEqualTo(37.557);
        assertThat(result.bounds().neLng()).isEqualTo(126.924);
        LockerKeywordItemResult item = result.items().getFirst();
        assertThat(item.suggestType()).isEqualTo(LockerSuggestType.PLACE);
        assertThat(item.latitude()).isEqualTo(37.557);
        assertThat(item.longitude()).isEqualTo(126.924);
        assertThat(item.isFavorite()).isNull();
        assertThat(item.lockers()).hasSize(1);
        assertThat(item.lockers().getFirst().lockerId()).isEqualTo(10L);
        assertThat(item.lockers().getFirst().latitude()).isEqualTo(37.556);
        assertThat(item.lockers().getFirst().longitude()).isEqualTo(126.923);
        assertThat(item.lockers().getFirst().isFavorite()).isFalse();
    }

    @Test
    @DisplayName("LOCKER 결과는 즐겨찾기 필드를 포함하고 하위 보관함 목록은 비운다")
    void returnsLockerWithFavoriteField() {
        LockerSuggestItemResult lockerItem = new LockerSuggestItemResult(
            LockerSuggestType.LOCKER,
            101L,
            "신촌역 1번 출구",
            10L,
            "신촌역 1번 출구 b1 관리사무소 옆",
            "서울 서대문구 신촌역로 1",
            "SUBWAY_STATION",
            37.556,
            126.923,
            95L,
            LocalDateTime.of(2026, 5, 31, 12, 0)
        );
        given(lockerSearchQueryService.search(37.55, 126.93, "신촌역1번출구b1", 10))
            .willReturn(List.of(lockerItem));

        LockerKeywordResult result = lockerKeywordQueryService.getKeywordResults(
            37.55,
            126.93,
            "신촌역1번출구b1",
            10
        );

        assertThat(result.count()).isEqualTo(1);
        assertThat(result.bounds().swLat()).isEqualTo(37.556);
        assertThat(result.bounds().swLng()).isEqualTo(126.923);
        assertThat(result.bounds().neLat()).isEqualTo(37.556);
        assertThat(result.bounds().neLng()).isEqualTo(126.923);
        LockerKeywordItemResult item = result.items().getFirst();
        assertThat(item.suggestType()).isEqualTo(LockerSuggestType.LOCKER);
        assertThat(item.lockerId()).isEqualTo(10L);
        assertThat(item.latitude()).isEqualTo(37.556);
        assertThat(item.longitude()).isEqualTo(126.923);
        assertThat(item.isFavorite()).isFalse();
        assertThat(item.lockers()).isEmpty();
        then(lockerPlaceLockerReader).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("keyword bounds는 하위 보관함을 제외한 대표 아이템 좌표로 계산한다")
    void calculatesBoundsFromCollapsedItemsOnly() {
        LockerSuggestItemResult placeItem = new LockerSuggestItemResult(
            LockerSuggestType.PLACE,
            101L,
            "신촌역 1번 출구",
            null,
            null,
            "서울 서대문구 신촌역로 1",
            null,
            37.557,
            126.924,
            100L,
            null
        );
        LockerSuggestItemResult lockerItem = new LockerSuggestItemResult(
            LockerSuggestType.LOCKER,
            102L,
            "홍대입구역",
            20L,
            "홍대입구역 2번 출구 보관함",
            "서울 마포구 양화로 1",
            "SUBWAY_STATION",
            37.551,
            126.936,
            200L,
            LocalDateTime.of(2026, 5, 31, 12, 0)
        );
        given(lockerSearchQueryService.search(37.55, 126.93, "역", 10))
            .willReturn(List.of(placeItem, lockerItem));
        given(lockerPlaceLockerReader.readByPlaceIds(37.55, 126.93, List.of(101L)))
            .willReturn(Map.of(
                101L,
                List.of(new LockerPlaceLocker(
                    101L,
                    10L,
                    "bounds에 포함하지 않는 하위 보관함",
                    "서울 서대문구 신촌역로 1",
                    "SUBWAY_STATION",
                    37.1,
                    126.1,
                    95L,
                    LocalDateTime.of(2026, 5, 31, 12, 0)
                ))
            ));

        LockerKeywordResult result = lockerKeywordQueryService.getKeywordResults(37.55, 126.93, "역", 10);

        assertThat(result.count()).isEqualTo(2);
        assertThat(result.bounds().swLat()).isEqualTo(37.551);
        assertThat(result.bounds().swLng()).isEqualTo(126.924);
        assertThat(result.bounds().neLat()).isEqualTo(37.557);
        assertThat(result.bounds().neLng()).isEqualTo(126.936);
        assertThat(result.items().getFirst().lockers().getFirst().latitude()).isEqualTo(37.1);
        assertThat(result.items().getFirst().lockers().getFirst().longitude()).isEqualTo(126.1);
    }
}
