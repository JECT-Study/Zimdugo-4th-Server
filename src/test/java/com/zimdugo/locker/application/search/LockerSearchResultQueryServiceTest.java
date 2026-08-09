package com.zimdugo.locker.application.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.application.result.search.LockerSearchItemResult;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;

@ExtendWith(MockitoExtension.class)
class LockerSearchResultQueryServiceTest {

    @Mock
    private LockerSearchDisplayQueryService displayQueryService;
    @Mock
    private SearchKeywordEventCommandService searchKeywordEventCommandService;

    @Test
    void recordsTheKeywordAndWrapsDisplayItemsAsASearchResult() {
        LockerSearchFilter filter = LockerSearchFilter.empty();
        List<LockerSearchItemResult> items = List.of(lockerItem());
        given(displayQueryService.getDisplayableItems(1L, 37.55, 126.93, "신촌", filter)).willReturn(items);
        LockerSearchResultQueryService service = new LockerSearchResultQueryService(
            displayQueryService,
            searchKeywordEventCommandService
        );

        var result = service.getSearchResults(1L, 37.55, 126.93, "신촌", filter);

        assertThat(result.count()).isEqualTo(1);
        assertThat(result.items()).containsExactlyElementsOf(items);
        verify(searchKeywordEventCommandService).record("신촌");
    }

    @Test
    void doesNotLogTheRawKeywordWhenKeywordEventRecordingFails() {
        String rawKeyword = "private-email@example.com";
        LockerSearchFilter filter = LockerSearchFilter.empty();
        given(displayQueryService.getDisplayableItems(1L, 37.55, 126.93, rawKeyword, filter))
            .willReturn(List.of(lockerItem()));
        willThrow(new DataAccessResourceFailureException("outbox unavailable"))
            .given(searchKeywordEventCommandService)
            .record(rawKeyword);
        Logger logger = (Logger) LoggerFactory.getLogger(LockerSearchResultQueryService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            new LockerSearchResultQueryService(displayQueryService, searchKeywordEventCommandService)
                .getSearchResults(1L, 37.55, 126.93, rawKeyword, filter);

            assertThat(appender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("키워드 집계 이벤트 저장에 실패해도 검색은 계속 진행합니다.")
                .noneMatch(message -> message.contains(rawKeyword));
        } finally {
            logger.detachAppender(appender);
        }
    }

    private LockerSearchItemResult lockerItem() {
        return new LockerSearchItemResult(
            LockerItemType.LOCKER, 100L, "신촌역", 10L, "신촌역 보관함", "서울 서대문구 신촌로 1",
            "SUBWAY_STATION", 1000, 37.55, 126.93, 100, LocalDateTime.of(2026, 8, 8, 12, 0), false,
            List.of()
        );
    }
}
