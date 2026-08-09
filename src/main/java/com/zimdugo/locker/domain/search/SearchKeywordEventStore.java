package com.zimdugo.locker.domain.search;

public interface SearchKeywordEventStore {

    void record(String rawKeyword, String normalizedKeyword);
}
