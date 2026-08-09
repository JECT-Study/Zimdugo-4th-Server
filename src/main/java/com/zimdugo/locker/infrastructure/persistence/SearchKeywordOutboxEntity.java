package com.zimdugo.locker.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "search_keyword_outbox",
    indexes = {
        @Index(name = "idx_search_keyword_outbox_pending", columnList = "status, searched_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchKeywordOutboxEntity {

    @Id
    private UUID id;

    @Column(name = "raw_keyword", nullable = false, length = 100)
    private String rawKeyword;

    @Column(name = "normalized_keyword", nullable = false, length = 100)
    private String normalizedKeyword;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SearchKeywordOutboxStatus status;

    @Column(name = "processed_at")
    private LocalDateTime processedAt; // 모니터링 용임.

    private SearchKeywordOutboxEntity(String rawKeyword, String normalizedKeyword, LocalDateTime searchedAt) {
        this.id = UUID.randomUUID();
        this.rawKeyword = rawKeyword;
        this.normalizedKeyword = normalizedKeyword;
        this.searchedAt = searchedAt;
        this.status = SearchKeywordOutboxStatus.PENDING;
    }

    public static SearchKeywordOutboxEntity pending(
        String rawKeyword,
        String normalizedKeyword,
        LocalDateTime searchedAt
    ) {
        return new SearchKeywordOutboxEntity(rawKeyword, normalizedKeyword, searchedAt);
    }
}
