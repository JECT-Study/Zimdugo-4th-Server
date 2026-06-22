package com.zimdugo.user.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record VisitorAccessEvent(
    String visitorIdentifier,
    Long userId,
    LocalDate accessedDate,
    LocalDateTime accessedAt
) {
}
