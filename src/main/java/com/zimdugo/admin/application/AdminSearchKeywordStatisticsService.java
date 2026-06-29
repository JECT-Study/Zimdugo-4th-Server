package com.zimdugo.admin.application;

import com.zimdugo.admin.application.dto.AdminSearchKeywordStatisticsResult;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zimdugo.locker.domain.search.SearchKeywordStatistic;
import com.zimdugo.locker.domain.search.SearchKeywordStatisticsReader;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminSearchKeywordStatisticsService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final SearchKeywordStatisticsReader searchKeywordStatisticsReader;

    public AdminSearchKeywordStatisticsResult getStatistics() {
        LocalDate today = LocalDate.now(SEOUL_ZONE);
        List<SearchKeywordStatistic> rows = searchKeywordStatisticsReader.readStatistics(today);
        List<AdminSearchKeywordStatisticsResult.Item> items = new ArrayList<>(rows.size());
        for (int index = 0; index < rows.size(); index++) {
            SearchKeywordStatistic row = rows.get(index);
            items.add(new AdminSearchKeywordStatisticsResult.Item(
                index + 1,
                row.keyword(),
                row.totalCount(),
                row.todayCount()
            ));
        }
        return new AdminSearchKeywordStatisticsResult(today, items);
    }
}
