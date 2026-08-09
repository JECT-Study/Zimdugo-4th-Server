package com.zimdugo.locker.application.search;

import static org.mockito.BDDMockito.then;

import com.zimdugo.locker.domain.search.SearchKeywordEventStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchKeywordEventCommandServiceTest {

    @Mock
    private SearchKeywordEventStore searchKeywordEventStore;

    @Test
    void recordsTheNormalizedKeywordAsAnOutboxEvent() {
        SearchKeywordEventCommandService service = new SearchKeywordEventCommandService(searchKeywordEventStore);

        service.record(" 신촌 역 ");

        then(searchKeywordEventStore).should().record("신촌 역", "신촌역");
    }

    @Test
    void ignoresAKeywordThatBecomesEmptyAfterNormalization() {
        SearchKeywordEventCommandService service = new SearchKeywordEventCommandService(searchKeywordEventStore);

        service.record(" \t\u00a0 ");

        then(searchKeywordEventStore).shouldHaveNoInteractions();
    }
}
