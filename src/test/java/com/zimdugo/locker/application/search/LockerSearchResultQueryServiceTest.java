package com.zimdugo.locker.application.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.application.result.search.LockerSearchItemResult;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LockerSearchResultQueryServiceTest {

    @Mock
    private LockerSearchDisplayQueryService displayQueryService;
    @Mock
    private SearchKeywordCountCommandService keywordCountCommandService;

    @Test
    void recordsTheKeywordAndWrapsDisplayItemsAsASearchResult() {
        LockerSearchFilter filter = LockerSearchFilter.empty();
        List<LockerSearchItemResult> items = List.of(lockerItem());
        given(displayQueryService.getDisplayableItems(1L, 37.55, 126.93, "신촌", filter)).willReturn(items);
        LockerSearchResultQueryService service = new LockerSearchResultQueryService(
            displayQueryService,
            keywordCountCommandService
        );

        var result = service.getSearchResults(1L, 37.55, 126.93, "신촌", filter);

        assertThat(result.count()).isEqualTo(1);
        assertThat(result.items()).containsExactlyElementsOf(items);
        verify(keywordCountCommandService).increase("신촌");
    }

    private LockerSearchItemResult lockerItem() {
        return new LockerSearchItemResult(
            LockerItemType.LOCKER, 100L, "신촌역", 10L, "신촌역 보관함", "서울 서대문구 신촌로 1",
            "SUBWAY_STATION", 1000, 37.55, 126.93, 100, LocalDateTime.of(2026, 8, 8, 12, 0), false,
            List.of()
        );
    }
}
