package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.translation.AdminLockerReportTranslationService;
import com.zimdugo.admin.translation.dto.AdminLockerReportTranslationPageResult;
import com.zimdugo.admin.translation.dto.AdminTranslationDraftResult;
import com.zimdugo.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/locker-reports")
@RequiredArgsConstructor
public class AdminLockerReportController {

    private final AdminLockerReportTranslationService translationService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reports", translationService.getRecentReports());
        model.addAttribute("activeMenu", "locker-reports");
        return "admin/locker-report-list";
    }

    @GetMapping("/{id}/translations")
    public String translations(@PathVariable(name = "id") Long id, Model model) {
        addTranslationPageModel(id, model);
        return "admin/locker-report-translations";
    }

    @PostMapping("/{id}/translations/draft")
    public String generateDraft(
        @PathVariable(name = "id") Long id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            AdminTranslationDraftResult draft = translationService.generateDraft(id);
            redirectAttributes.addFlashAttribute("draft", draft);
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("draftError", e.getMessage());
        }
        return "redirect:/admin/locker-reports/" + id + "/translations";
    }

    private void addTranslationPageModel(Long id, Model model) {
        AdminLockerReportTranslationPageResult page = translationService.getTranslationPage(id);
        model.addAttribute("page", page);
        model.addAttribute("activeMenu", "locker-reports");
    }
}
