package com.zimdugo.locker.domain.search;

import java.time.LocalDate;
import java.util.List;

public interface SearchKeywordStatisticsReader {
    List<SearchKeywordStatistic> readStatistics(LocalDate today);
}
