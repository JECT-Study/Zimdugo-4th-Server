package com.zimdugo.locker.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "keyword_daily_counts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_keyword_daily_counts_keyword_date",
            columnNames = {"keyword", "stat_date"}
        )
    },
    indexes = {
        @Index(name = "idx_keyword_daily_counts_stat_date", columnList = "stat_date")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchKeywordDailyCountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String keyword;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "search_count", nullable = false)
    private Long count;
}
