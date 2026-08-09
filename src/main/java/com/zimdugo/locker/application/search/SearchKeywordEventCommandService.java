package com.zimdugo.locker.application.search;

import com.zimdugo.common.i18n.SearchTextNormalizer;
import com.zimdugo.locker.domain.search.SearchKeywordEventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchKeywordEventCommandService {

    private final SearchKeywordEventStore searchKeywordEventStore;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String rawKeyword) {
        String normalizedKeyword = SearchTextNormalizer.normalize(rawKeyword);
        if (normalizedKeyword.isBlank()) {
            return;
        }
        searchKeywordEventStore.record(rawKeyword.trim(), normalizedKeyword);
    }
}
