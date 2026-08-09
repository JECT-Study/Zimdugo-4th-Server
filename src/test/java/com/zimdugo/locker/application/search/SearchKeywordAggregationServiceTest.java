package com.zimdugo.locker.application.search;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.zimdugo.locker.domain.search.SearchKeywordCountStore;
import com.zimdugo.locker.domain.search.SearchKeywordDailyCountStore;
import com.zimdugo.locker.domain.search.SearchKeywordOutboxEvent;
import com.zimdugo.locker.domain.search.SearchKeywordOutboxStore;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchKeywordAggregationServiceTest {

    @Mock
    private SearchKeywordOutboxStore searchKeywordOutboxStore;
    @Mock
    private SearchKeywordCountStore searchKeywordCountStore;
    @Mock
    private SearchKeywordDailyCountStore searchKeywordDailyCountStore;

    @Test
    void aggregatesMatchingKeywordsAndCompletesTheProcessedEvents() {
        UUID firstEventId = UUID.randomUUID();
        UUID secondEventId = UUID.randomUUID();
        LocalDateTime searchedAt = LocalDateTime.of(2026, 8, 8, 10, 0);
        given(searchKeywordOutboxStore.claimPendingEvents(100)).willReturn(List.of(
            new SearchKeywordOutboxEvent(firstEventId, "신촌 역", "신촌역", searchedAt),
            new SearchKeywordOutboxEvent(secondEventId, "신촌역", "신촌역", searchedAt)
        ));
        SearchKeywordAggregationService service = new SearchKeywordAggregationService(
            searchKeywordOutboxStore,
            searchKeywordCountStore,
            searchKeywordDailyCountStore
        );

        int processedCount = service.aggregatePendingEvents();

        then(searchKeywordCountStore).should().increase("신촌역", 2);
        then(searchKeywordDailyCountStore).should().increase("신촌역", searchedAt.toLocalDate(), 2);
        then(searchKeywordOutboxStore).should().complete(List.of(firstEventId, secondEventId));
        org.assertj.core.api.Assertions.assertThat(processedCount).isEqualTo(2);
    }
}
