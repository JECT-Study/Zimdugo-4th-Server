package com.zimdugo.locker.domain.search;

public record SearchKeywordStatistic(
    String keyword,
    long totalCount,
    long todayCount
) {
}
