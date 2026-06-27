package com.zimdugo.locker.application.keyword;

import com.zimdugo.locker.domain.keyword.KeywordCountStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KeywordCountCommandService {

    private final KeywordCountStore keywordCountStore;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increase(String keyword) {
        String normalizedKeyword = normalize(keyword);
        if (normalizedKeyword == null) {
            return;
        }
        keywordCountStore.increase(normalizedKeyword);
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
