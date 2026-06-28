package com.zimdugo.locker.application.search;

import com.zimdugo.locker.domain.search.SearchKeywordCountStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchKeywordCountCommandService {

    private final SearchKeywordCountStore searchKeywordCountStore;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increase(String keyword) {
        String normalizedKeyword = normalize(keyword);
        if (normalizedKeyword == null) {
            return;
        }
        searchKeywordCountStore.increase(normalizedKeyword);
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
