package com.zimdugo.user.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface VisitorLogRepository {
    void saveAccessLog(String visitorIdentifier, Long userId, LocalDate accessedDate, LocalDateTime accessedAt);
    long countVisitorsByDate(LocalDate date);
}
