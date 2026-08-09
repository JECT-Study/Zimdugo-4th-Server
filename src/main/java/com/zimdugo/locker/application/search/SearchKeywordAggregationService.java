package com.zimdugo.locker.application.search;

import com.zimdugo.locker.domain.search.SearchKeywordCountStore;
import com.zimdugo.locker.domain.search.SearchKeywordDailyCountStore;
import com.zimdugo.locker.domain.search.SearchKeywordOutboxEvent;
import com.zimdugo.locker.domain.search.SearchKeywordOutboxStore;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchKeywordAggregationService {

    private static final int DEFAULT_BATCH_SIZE = 100;

    private final SearchKeywordOutboxStore searchKeywordOutboxStore;
    private final SearchKeywordCountStore searchKeywordCountStore;
    private final SearchKeywordDailyCountStore searchKeywordDailyCountStore;

    @Transactional
    public int aggregatePendingEvents() {
        List<SearchKeywordOutboxEvent> events = searchKeywordOutboxStore.claimPendingEvents(DEFAULT_BATCH_SIZE);
        Map<SearchKeywordStatKey, Long> counts = events.stream()
            .collect(Collectors.groupingBy(
                event -> new SearchKeywordStatKey(event.normalizedKeyword(), event.searchedAt().toLocalDate()),
                Collectors.counting()
            ));
        counts.forEach((key, count) -> {
            searchKeywordCountStore.increase(key.keyword(), count);
            searchKeywordDailyCountStore.increase(key.keyword(), key.statDate(), count);
        });
        List<UUID> eventIds = events.stream().map(SearchKeywordOutboxEvent::id).toList();
        if (!eventIds.isEmpty()) {
            searchKeywordOutboxStore.complete(eventIds);
        }
        return events.size();
    }

    private record SearchKeywordStatKey(String keyword, LocalDate statDate) {
    }
}
