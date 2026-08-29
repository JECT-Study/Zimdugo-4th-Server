package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.entrypoint.dto.AdminMaintenanceNoticeForm;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.maintenance.application.MaintenanceNoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/maintenance-notice")
@RequiredArgsConstructor
public class AdminMaintenanceNoticeController {

    private static final String FORM_VIEW = "admin/maintenance-notice-form";

    private final MaintenanceNoticeService maintenanceNoticeService;

    @GetMapping
    public String form(final Model model) {
        model.addAttribute("form", AdminMaintenanceNoticeForm.from(maintenanceNoticeService.getAdminNotice()));
        model.addAttribute("activeMenu", "maintenance");
        return FORM_VIEW;
    }

    @PostMapping
    public String update(
        @Valid @ModelAttribute("form") final AdminMaintenanceNoticeForm form,
        final BindingResult bindingResult,
        final Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "maintenance");
            return FORM_VIEW;
        }

        try {
            maintenanceNoticeService.update(form.toCommand());
        } catch (final BusinessException exception) {
            bindingResult.rejectValue("endedAt", "maintenanceNotice.invalidPeriod", exception.getMessage());
            model.addAttribute("activeMenu", "maintenance");
            return FORM_VIEW;
        }

        return "redirect:/admin/maintenance-notice";
    }
}
