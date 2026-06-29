package com.zimdugo.locker.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchKeywordCountRepository extends JpaRepository<SearchKeywordCountEntity, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO keyword_counts (keyword, search_count)
        VALUES (:keyword, 1)
        ON CONFLICT (keyword)
        DO UPDATE SET search_count = keyword_counts.search_count + 1
        """, nativeQuery = true)
    void increase(@Param("keyword") String keyword);

    @Query(value = """
        SELECT
            kc.keyword AS keyword,
            kc.search_count AS totalCount,
            COALESCE(kdc.search_count, 0) AS todayCount
        FROM keyword_counts kc
        LEFT JOIN keyword_daily_counts kdc
            ON kdc.keyword = kc.keyword
           AND kdc.stat_date = :today
        ORDER BY kc.search_count DESC, COALESCE(kdc.search_count, 0) DESC, kc.keyword ASC
        """, nativeQuery = true)
    List<AdminSearchKeywordStatisticsProjection> findStatistics(@Param("today") LocalDate today);
}
