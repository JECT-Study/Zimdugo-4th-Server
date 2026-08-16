package com.zimdugo.locker.application.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.zimdugo.common.i18n.CurrentRequestLanguage;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.domain.favorite.FavoriteLockerReader;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.place.LockerPlaceLocker;
import com.zimdugo.locker.domain.place.LockerPlaceLockerReader;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LockerSearchDisplayQueryServiceTest {

    @Mock
    private LockerSearchTargetQueryService targetQueryService;
    @Mock
    private LockerPlaceLockerReader placeLockerReader;
    @Mock
    private FavoriteLockerReader favoriteLockerReader;
    @Mock
    private CurrentRequestLanguage currentRequestLanguage;

    @Test
    void enrichesAPlaceTargetWithFilteredLockersAndFavorites() {
        LockerSearchFilter filter = LockerSearchFilter.empty();
        given(targetQueryService.findTargets(37.55, 126.93, "신촌", filter, 43)).willReturn(List.of(placeTarget()));
        given(currentRequestLanguage.resolve()).willReturn(SupportedLanguage.KOREAN);
        given(placeLockerReader.readByPlaceIds(37.55, 126.93, List.of(100L), filter, "ko")).willReturn(
            Map.of(100L, List.of(placeLocker()))
        );
        given(favoriteLockerReader.findFavoriteLockerIds(1L, Set.of(10L))).willReturn(Set.of(10L));
        LockerSearchDisplayQueryService service = new LockerSearchDisplayQueryService(
            targetQueryService,
            placeLockerReader,
            favoriteLockerReader,
            currentRequestLanguage,
            43
        );

        var items = service.getDisplayableItems(1L, 37.55, 126.93, "신촌", filter);

        assertThat(items).singleElement().satisfies(item -> {
            assertThat(item.type()).isEqualTo(LockerItemType.PLACE);
            assertThat(item.lockers()).singleElement().satisfies(locker -> {
                assertThat(locker.lockerId()).isEqualTo(10L);
                assertThat(locker.isFavorite()).isTrue();
            });
        });
    }

    @Test
    void removesAPlaceTargetWhenNoLockerMatchesTheFilter() {
        LockerSearchFilter filter = LockerSearchFilter.empty();
        given(targetQueryService.findTargets(37.55, 126.93, "신촌", filter, 43)).willReturn(List.of(placeTarget()));
        given(currentRequestLanguage.resolve()).willReturn(SupportedLanguage.KOREAN);
        given(placeLockerReader.readByPlaceIds(37.55, 126.93, List.of(100L), filter, "ko")).willReturn(Map.of());
        LockerSearchDisplayQueryService service = new LockerSearchDisplayQueryService(
            targetQueryService,
            placeLockerReader,
            favoriteLockerReader,
            currentRequestLanguage,
            43
        );

        var items = service.getDisplayableItems(null, 37.55, 126.93, "신촌", filter);

        assertThat(items).isEmpty();
    }

    private LockerSearchTarget placeTarget() {
        return new LockerSearchTarget(
            LockerItemType.PLACE, 100L, "신촌역 1번 출구", null, null, "서울 서대문구 신촌로 1",
            null, null, 37.55, 126.93, 100, null
        );
    }

    private LockerPlaceLocker placeLocker() {
        return new LockerPlaceLocker(
            100L, 10L, "신촌역 1번 출구 보관함", "서울 서대문구 신촌로 1", LockerType.SUBWAY_STATION,
            IndoorOutdoorType.INDOOR, Set.of(LockerSizeType.MEDIUM), 1000, 37.55, 126.93, 100,
            LocalDateTime.of(2026, 8, 8, 12, 0)
        );
    }
}
