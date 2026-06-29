package com.zimdugo.locker.infrastructure.persistence;

public interface AdminSearchKeywordStatisticsProjection {
    String getKeyword();
    long getTotalCount();
    long getTodayCount();
}
