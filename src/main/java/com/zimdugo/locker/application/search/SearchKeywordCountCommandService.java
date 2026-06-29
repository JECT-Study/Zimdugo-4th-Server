package com.zimdugo.locker.application.search;

import com.zimdugo.locker.domain.search.SearchKeywordDailyCountStore;
import com.zimdugo.locker.domain.search.SearchKeywordCountStore;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchKeywordCountCommandService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final SearchKeywordCountStore searchKeywordCountStore;
    private final SearchKeywordDailyCountStore searchKeywordDailyCountStore;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increase(String keyword) {
        String normalizedKeyword = normalize(keyword);
        if (normalizedKeyword == null) {
            return;
        }
        searchKeywordCountStore.increase(normalizedKeyword);
        searchKeywordDailyCountStore.increase(normalizedKeyword, LocalDate.now(SEOUL_ZONE));
    }

    private String normalize(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }
}
