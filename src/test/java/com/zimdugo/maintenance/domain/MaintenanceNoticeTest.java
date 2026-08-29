package com.zimdugo.maintenance.domain;

import com.zimdugo.core.exception.BusinessException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaintenanceNoticeTest {

    @Test
    @DisplayName("토글이 켜져 있으면 표시 시간과 관계없이 점검 중이다")
    void enabledControlsMaintenanceStateIndependentlyOfDisplayTimes() {
        LocalDateTime startedAt = LocalDateTime.of(2026, 8, 30, 1, 0);
        LocalDateTime endedAt = startedAt.plusHours(1);

        MaintenanceNotice notice = MaintenanceNotice.of(
            true,
            "점검 중입니다",
            "서비스를 점검하고 있습니다.",
            startedAt,
            endedAt
        );

        assertThat(notice.enabled()).isTrue();
        assertThat(notice.startedAt()).isEqualTo(startedAt);
        assertThat(notice.endedAt()).isEqualTo(endedAt);
    }

    @Test
    @DisplayName("종료 시간은 시작 시간보다 빠를 수 없다")
    void rejectsEndTimeBeforeStartTime() {
        LocalDateTime startedAt = LocalDateTime.of(2026, 8, 30, 1, 0);

        assertThatThrownBy(() -> MaintenanceNotice.of(
            false,
            "점검 예정입니다",
            "서비스를 점검할 예정입니다.",
            startedAt,
            startedAt.minusMinutes(1)
        )).isInstanceOf(BusinessException.class);
    }
}
