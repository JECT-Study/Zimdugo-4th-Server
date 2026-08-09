package com.zimdugo.locker.application.search;

import com.zimdugo.locker.application.filter.LockerSearchFilterFactory;
import com.zimdugo.locker.application.result.search.LockerSearchItemResult;
import com.zimdugo.locker.application.result.search.LockerSearchResult;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LockerSearchResultQueryService {

    private final LockerSearchDisplayQueryService lockerSearchDisplayQueryService;
    private final SearchKeywordEventCommandService searchKeywordEventCommandService;

    public LockerSearchResult getSearchResults(Long userId, LockerSearchCommand command) {
        return getSearchResults(
            userId,
            command.latitude(),
            command.longitude(),
            command.keyword(),
            LockerSearchFilterFactory.create(
                command.sizeTypes(),
                command.indoorOutdoorTypes(),
                command.lockerTypes()
            )
        );
    }

    public LockerSearchResult getSearchResults(
        Long userId,
        double latitude,
        double longitude,
        String keyword,
        LockerSearchFilter filter
    ) {
        recordSearchKeywordEvent(keyword);
        List<LockerSearchItemResult> items = lockerSearchDisplayQueryService.getDisplayableItems(
            userId,
            latitude,
            longitude,
            keyword,
            filter
        );
        return LockerSearchResult.of(items);
    }

    private void recordSearchKeywordEvent(String keyword) {
        try {
            searchKeywordEventCommandService.record(keyword);
        } catch (DataAccessException | TransactionException exception) {
            log.warn("키워드 집계 이벤트 저장에 실패해도 검색은 계속 진행합니다.", exception);
        }
    }
}
