package com.zimdugo.admin.application.dto;

import java.time.LocalDate;
import java.util.List;

public record AdminSearchKeywordStatisticsResult(
    LocalDate today,
    List<Item> items
) {

    public record Item(
        int rank,
        String keyword,
        long totalCount,
        long todayCount
    ) {
    }
}
