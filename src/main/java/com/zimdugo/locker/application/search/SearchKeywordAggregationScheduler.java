package com.zimdugo.locker.application.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchKeywordAggregationScheduler {

    private final SearchKeywordAggregationService searchKeywordAggregationService;

    @Scheduled(fixedDelayString = "${search.keyword-aggregation.fixed-delay-millis}")
    public void aggregatePendingEvents() {
        try {
            int processedCount = searchKeywordAggregationService.aggregatePendingEvents();
            if (processedCount > 0) {
                log.debug("검색어 Outbox 집계 완료. processedCount={}", processedCount);
            }
        } catch (Exception exception) {
            log.error("검색어 Outbox 집계에 실패했습니다.", exception);
        }
    }
}
