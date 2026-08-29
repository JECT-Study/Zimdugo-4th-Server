package com.zimdugo.admin.entrypoint.dto;

import com.zimdugo.maintenance.application.dto.AdminMaintenanceNoticeResult;
import com.zimdugo.maintenance.application.dto.MaintenanceNoticeUpdateCommand;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminMaintenanceNoticeFormTest {

    @Test
    @DisplayName("저장된 점검 공지는 관리자 폼 값으로 변환한다")
    void createsFormFromSavedNotice() {
        LocalDateTime startedAt = LocalDateTime.of(2026, 8, 30, 1, 0);
        AdminMaintenanceNoticeForm form = AdminMaintenanceNoticeForm.from(new AdminMaintenanceNoticeResult(
            true,
            "서비스 점검 중입니다",
            "점검 안내 문구입니다.",
            startedAt,
            null
        ));

        MaintenanceNoticeUpdateCommand command = form.toCommand();

        assertThat(command.enabled()).isTrue();
        assertThat(command.title()).isEqualTo("서비스 점검 중입니다");
        assertThat(command.message()).isEqualTo("점검 안내 문구입니다.");
        assertThat(command.startedAt()).isEqualTo(startedAt);
        assertThat(command.endedAt()).isNull();
    }
}
