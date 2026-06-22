package com.zimdugo.user.infrastructure.adapter;

import com.zimdugo.user.domain.VisitorLogRepository;
import com.zimdugo.user.infrastructure.persistence.JpaVisitorLogRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VisitorLogRepositoryAdapter implements VisitorLogRepository {

    private final JpaVisitorLogRepository jpaVisitorLogRepository;

    @Override
    public void saveAccessLog(String visitorIdentifier, Long userId, LocalDate accessedDate, LocalDateTime accessedAt) {
        jpaVisitorLogRepository.saveVisitorLog(visitorIdentifier, accessedDate, accessedAt, userId);
    }

    @Override
    public long countVisitorsBetween(LocalDateTime start, LocalDateTime end) {
        return jpaVisitorLogRepository.countVisitorsBetween(start, end);
    }
}
