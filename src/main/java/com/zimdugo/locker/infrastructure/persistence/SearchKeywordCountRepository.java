package com.zimdugo.locker.infrastructure.persistence;

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
}
