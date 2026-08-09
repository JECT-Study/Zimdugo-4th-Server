package com.zimdugo.locker.domain.search;

import java.time.LocalDateTime;
import java.util.UUID;

public record SearchKeywordOutboxEvent(
    UUID id,
    String rawKeyword,
    String normalizedKeyword,
    LocalDateTime searchedAt
) {
}
