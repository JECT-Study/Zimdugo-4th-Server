package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.entrypoint.dto.AdminMaintenanceNoticeForm;
import com.zimdugo.maintenance.application.MaintenanceNoticeService;
import com.zimdugo.maintenance.application.dto.AdminMaintenanceNoticeResult;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AdminMaintenanceNoticeControllerTest {

    @Test
    @DisplayName("점검 설정 화면은 저장된 값을 폼에 채워 렌더링한다")
    void rendersMaintenanceNoticeFormWithSavedValues() {
        MaintenanceNoticeService maintenanceNoticeService = mock(MaintenanceNoticeService.class);
        LocalDateTime startedAt = LocalDateTime.of(2026, 8, 30, 1, 0);
        given(maintenanceNoticeService.getAdminNotice()).willReturn(new AdminMaintenanceNoticeResult(
            true,
            "서비스 점검 중입니다",
            "점검 안내 문구입니다.",
            startedAt,
            null
        ));
        AdminMaintenanceNoticeController controller = new AdminMaintenanceNoticeController(maintenanceNoticeService);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.form(model);

        assertThat(view).isEqualTo("admin/maintenance-notice-form");
        assertThat(model.getAttribute("activeMenu")).isEqualTo("maintenance");
        assertThat(model.getAttribute("form"))
            .isInstanceOfSatisfying(AdminMaintenanceNoticeForm.class, form -> {
                assertThat(form.isEnabled()).isTrue();
                assertThat(form.getTitle()).isEqualTo("서비스 점검 중입니다");
                assertThat(form.getStartedAt()).isEqualTo(startedAt);
                assertThat(form.getEndedAt()).isNull();
            });
    }

    @Test
    @DisplayName("점검 설정을 저장한 뒤 설정 화면으로 이동한다")
    void updatesMaintenanceNoticeAndRedirectsToForm() {
        MaintenanceNoticeService maintenanceNoticeService = mock(MaintenanceNoticeService.class);
        AdminMaintenanceNoticeController controller = new AdminMaintenanceNoticeController(maintenanceNoticeService);
        AdminMaintenanceNoticeForm form = new AdminMaintenanceNoticeForm();
        form.setEnabled(true);
        form.setTitle("서비스 점검 중입니다");
        form.setMessage("점검 안내 문구입니다.");
        form.setStartedAt(LocalDateTime.of(2026, 8, 30, 1, 0));

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        String view = controller.update(
            form,
            new BeanPropertyBindingResult(form, "form"),
            new ExtendedModelMap(),
            redirectAttributes
        );

        assertThat(view).isEqualTo("redirect:/admin/maintenance-notice");
        assertThat(redirectAttributes.getFlashAttributes().get("successMessage"))
            .isEqualTo("점검 설정을 적용했습니다.");
        verify(maintenanceNoticeService).update(form.toCommand());
    }
}
