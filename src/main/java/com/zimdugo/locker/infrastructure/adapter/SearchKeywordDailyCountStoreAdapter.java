package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.search.SearchKeywordDailyCountStore;
import com.zimdugo.locker.infrastructure.persistence.SearchKeywordDailyCountRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchKeywordDailyCountStoreAdapter implements SearchKeywordDailyCountStore {

    private final SearchKeywordDailyCountRepository searchKeywordDailyCountRepository;

    @Override
    public void increase(String keyword, LocalDate statDate) {
        searchKeywordDailyCountRepository.increase(keyword, statDate);
    }
}
