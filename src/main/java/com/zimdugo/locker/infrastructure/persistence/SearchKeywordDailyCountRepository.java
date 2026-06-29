package com.zimdugo.locker.infrastructure.persistence;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchKeywordDailyCountRepository extends JpaRepository<SearchKeywordDailyCountEntity, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO keyword_daily_counts (keyword, stat_date, search_count)
        VALUES (:keyword, :statDate, 1)
        ON CONFLICT (keyword, stat_date)
        DO UPDATE SET search_count = keyword_daily_counts.search_count + 1
        """, nativeQuery = true)
    void increase(@Param("keyword") String keyword, @Param("statDate") LocalDate statDate);
}
