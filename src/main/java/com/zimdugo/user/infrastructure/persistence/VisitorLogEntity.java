package com.zimdugo.user.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "visitor_logs",
    indexes = @Index(
        name = "idx_visitor_logs_accessed_date",
        columnList = "accessed_date"
    ),
    uniqueConstraints = @UniqueConstraint(
        name = "uk_visitor_logs_identifier_date",
        columnNames = {"visitor_identifier", "accessed_date"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VisitorLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visitor_identifier", nullable = false, length = 100)
    private String visitorIdentifier;

    @Column(name = "accessed_date", nullable = false)
    private LocalDate accessedDate;

    @Column(name = "accessed_at", nullable = false, columnDefinition = "timestamp(6)")
    private LocalDateTime accessedAt;

    @Column(name = "user_id")
    private Long userId;

    public VisitorLogEntity(String visitorIdentifier, LocalDate accessedDate, LocalDateTime accessedAt, Long userId) {
        this.visitorIdentifier = visitorIdentifier;
        this.accessedDate = accessedDate;
        this.accessedAt = accessedAt;
        this.userId = userId;
    }
}
