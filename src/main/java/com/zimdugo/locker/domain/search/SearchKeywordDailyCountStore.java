package com.zimdugo.locker.domain.search;

import java.time.LocalDate;

public interface SearchKeywordDailyCountStore {
    void increase(String keyword, LocalDate statDate);
}
