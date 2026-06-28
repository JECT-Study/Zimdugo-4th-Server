package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.search.SearchKeywordCountStore;
import com.zimdugo.locker.infrastructure.persistence.SearchKeywordCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchKeywordCountStoreAdapter implements SearchKeywordCountStore {

    private final SearchKeywordCountRepository searchKeywordCountRepository;

    @Override
    public void increase(String keyword) {
        searchKeywordCountRepository.increase(keyword);
    }
}
