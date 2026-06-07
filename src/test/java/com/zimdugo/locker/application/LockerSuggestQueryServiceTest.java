package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestResult;
import com.zimdugo.locker.application.result.LockerItemType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LockerSuggestQueryServiceTest {

    @Mock
    private LockerSearchQueryService lockerSearchQueryService;

    @InjectMocks
    private LockerSuggestQueryService lockerSuggestQueryService;

    @Test
    @DisplayName("검색 결과가 없으면 빈 응답을 반환한다")
    void returnsEmptyWhenNoSearchItems() {
        given(lockerSearchQueryService.search(37.55, 126.93, "신촌")).willReturn(List.of());

        LockerSuggestResult result = lockerSuggestQueryService.getSuggestions(37.55, 126.93, "신촌");

        assertThat(result.count()).isZero();
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("검색 결과가 있으면 suggest 응답으로 래핑한다")
    void wrapsSearchItemsToSuggestResult() {
        LockerSuggestItemResult item = new LockerSuggestItemResult(
            LockerItemType.LOCKER,
            101L,
            "신촌역 1번 출구",
            10L,
            "신촌역 1번 출구 b1 관리사무소 옆",
            "서울 서대문구 신촌역로 1",
            "SUBWAY_STATION",
            1000,
            37.556,
            126.923,
            95L,
            LocalDateTime.of(2026, 5, 31, 12, 0)
        );
        given(lockerSearchQueryService.search(37.55, 126.93, "신촌")).willReturn(List.of(item));

        LockerSuggestResult result = lockerSuggestQueryService.getSuggestions(37.55, 126.93, "신촌");

        assertThat(result.count()).isEqualTo(1);
        assertThat(result.items()).containsExactly(item);
    }
}
