package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.keyword.KeywordCountStore;
import com.zimdugo.locker.infrastructure.persistence.KeywordCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeywordCountStoreAdapter implements KeywordCountStore {

    private final KeywordCountRepository keywordCountRepository;

    @Override
    public void increase(String keyword) {
        keywordCountRepository.increase(keyword);
    }
}
