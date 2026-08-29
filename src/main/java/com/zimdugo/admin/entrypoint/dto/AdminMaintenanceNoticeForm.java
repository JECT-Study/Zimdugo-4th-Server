package com.zimdugo.admin.entrypoint.dto;

import com.zimdugo.maintenance.application.dto.AdminMaintenanceNoticeResult;
import com.zimdugo.maintenance.application.dto.MaintenanceNoticeUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminMaintenanceNoticeForm {

    private boolean enabled;

    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "안내 문구를 입력해 주세요.")
    @Size(max = 2000, message = "안내 문구는 2000자 이하여야 합니다.")
    private String message;

    @NotNull(message = "점검 시작 시간을 입력해 주세요.")
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    public static AdminMaintenanceNoticeForm from(final AdminMaintenanceNoticeResult result) {
        final AdminMaintenanceNoticeForm form = new AdminMaintenanceNoticeForm();
        form.setEnabled(result.enabled());
        form.setTitle(result.title());
        form.setMessage(result.message());
        form.setStartedAt(
            result.startedAt() == null ? LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES) : result.startedAt()
        );
        form.setEndedAt(result.endedAt());
        return form;
    }

    public MaintenanceNoticeUpdateCommand toCommand() {
        return new MaintenanceNoticeUpdateCommand(enabled, title, message, startedAt, endedAt);
    }
}
