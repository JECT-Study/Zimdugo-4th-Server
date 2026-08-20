package com.zimdugo.locker.application.suggest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.application.search.LockerSearchTarget;
import com.zimdugo.locker.application.search.LockerSearchTargetQueryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LockerSuggestQueryServiceTest {

    @Mock
    private LockerSearchTargetQueryService targetQueryService;

    @Test
    void projectsACommonSearchTargetIntoASuggestionItem() {
        given(targetQueryService.findTargets(37.55, 126.93, "신촌", null, 17)).willReturn(List.of(target()));
        LockerSuggestQueryService service = new LockerSuggestQueryService(
            targetQueryService,
            17
        );

        var result = service.getSuggestions(37.55, 126.93, "신촌");

        assertThat(result.count()).isEqualTo(1);
        assertThat(result.items()).singleElement().satisfies(item -> {
            assertThat(item.placeName()).isEqualTo("신촌역 1번 출구");
            assertThat(item.lockerName()).isEqualTo("신촌역 1번 출구 보관함");
            assertThat(item.distanceMeters()).isEqualTo(100);
        });
    }

    private LockerSearchTarget target() {
        return new LockerSearchTarget(
            LockerItemType.LOCKER, 100L, "신촌역 1번 출구", 10L, "신촌역 1번 출구 보관함",
            "서울 서대문구 신촌로 1", "SUBWAY_STATION", 1000, 37.55, 126.93, 100,
            LocalDateTime.of(2026, 8, 7, 12, 0)
        );
    }
}
