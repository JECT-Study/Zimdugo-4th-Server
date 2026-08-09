package com.zimdugo.locker.application.search;

import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchKeywordAggregationSchedulerTest {

    @Mock
    private SearchKeywordAggregationService searchKeywordAggregationService;

    @Test
    void delegatesScheduledWorkToTheAggregationService() {
        SearchKeywordAggregationScheduler scheduler = new SearchKeywordAggregationScheduler(
            searchKeywordAggregationService
        );

        scheduler.aggregatePendingEvents();

        then(searchKeywordAggregationService).should().aggregatePendingEvents();
    }
}
