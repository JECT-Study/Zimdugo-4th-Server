package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.search.SearchKeywordStatistic;
import com.zimdugo.locker.domain.search.SearchKeywordStatisticsReader;
import com.zimdugo.locker.infrastructure.persistence.AdminSearchKeywordStatisticsProjection;
import com.zimdugo.locker.infrastructure.persistence.SearchKeywordCountRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSearchKeywordStatisticsReaderAdapter implements SearchKeywordStatisticsReader {

    private final SearchKeywordCountRepository searchKeywordCountRepository;

    @Override
    public List<SearchKeywordStatistic> readStatistics(LocalDate today) {
        return searchKeywordCountRepository.findStatistics(today).stream()
            .map(this::toStatistic)
            .toList();
    }

    private SearchKeywordStatistic toStatistic(AdminSearchKeywordStatisticsProjection projection) {
        return new SearchKeywordStatistic(
            projection.getKeyword(),
            projection.getTotalCount(),
            projection.getTodayCount()
        );
    }
}
