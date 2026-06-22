package com.zimdugo.user.application;

import com.zimdugo.user.domain.VisitorAccessEvent;
import com.zimdugo.user.domain.VisitorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitorLogService {

    private final VisitorLogRepository visitorLogRepository;

    @Async
    @EventListener
    @Transactional
    public void handleVisitorAccessEvent(VisitorAccessEvent event) {
        try {
            visitorLogRepository.saveAccessLog(
                event.visitorIdentifier(),
                event.userId(),
                event.accessedDate(),
                event.accessedAt()
            );
        } catch (DataAccessException exception) {
            log.error("접속 로그 RDB 적재 실패 (데이터베이스 오류) [visitor: {}]", event.visitorIdentifier(), exception);
        } catch (Exception exception) {
            log.error("접속 로그 기록 실패 (예상치 못한 시스템 오류) [visitor: {}]", event.visitorIdentifier(), exception);
        }
    }
}
