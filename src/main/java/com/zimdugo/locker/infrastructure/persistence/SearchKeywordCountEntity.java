package com.zimdugo.locker.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "keyword_counts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_keyword_counts_keyword", columnNames = "keyword")
    },
    indexes = {
        @Index(name = "idx_keyword_counts_keyword", columnList = "keyword")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchKeywordCountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String keyword;

    @Column(name = "search_count", nullable = false)
    private Long count;
}
