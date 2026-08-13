package com.zimdugo.locker.application.realtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockerRealtimeAvailabilityScheduler {

    private final LockerRealtimeAvailabilitySyncService service;

    @Scheduled(fixedDelayString = "${seoul-metro.locker-api.sync-fixed-delay-millis:300000}")
    public void sync() {
        try {
            log.debug("서울교통공사 보관함 실시간 상태 갱신 완료. mappedCount={}", service.sync());
        } catch (Exception exception) {
            log.error("서울교통공사 보관함 실시간 상태 갱신에 실패했습니다.", exception);
        }
    }
}
