package com.zimdugo.locker.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchKeywordOutboxRepository extends JpaRepository<SearchKeywordOutboxEntity, UUID> {

    @Query(value = """
        SELECT *
        FROM search_keyword_outbox
        WHERE status = 'PENDING'
        ORDER BY searched_at, id
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<SearchKeywordOutboxEntity> lockPendingEvents(@Param("batchSize") int batchSize);

    @Modifying
    @Query(value = """
        UPDATE search_keyword_outbox
        SET status = 'PROCESSED', processed_at = :processedAt
        WHERE id IN :eventIds AND status = 'PENDING'
        """, nativeQuery = true)
    void complete(
        @Param("eventIds") List<UUID> eventIds,
        @Param("processedAt") LocalDateTime processedAt
    );
}
