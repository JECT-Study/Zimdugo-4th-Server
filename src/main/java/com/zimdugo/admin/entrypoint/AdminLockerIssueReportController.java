package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.entrypoint.dto.AdminLockerIssueReportReviewForm;
import com.zimdugo.admin.issue.AdminLockerIssueReportService;
import com.zimdugo.core.exception.BusinessException;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/locker-issue-reports")
@RequiredArgsConstructor
public class AdminLockerIssueReportController {

    private final AdminLockerIssueReportService adminLockerIssueReportService;

    @GetMapping
    public String list(
        @RequestParam(name = "status", required = false) String status,
        Model model
    ) {
        model.addAttribute("reports", adminLockerIssueReportService.getReports(status));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", adminLockerIssueReportService.getStatusOptions());
        model.addAttribute("activeMenu", "locker-issue-reports");
        return "admin/locker-issue-report-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model) {
        model.addAttribute("report", adminLockerIssueReportService.getReport(id));
        if (!model.containsAttribute("reviewForm")) {
            model.addAttribute("reviewForm", new AdminLockerIssueReportReviewForm());
        }
        model.addAttribute("activeMenu", "locker-issue-reports");
        return "admin/locker-issue-report-detail";
    }

    @PostMapping("/{id}/resolve")
    @SuppressWarnings("checkstyle:ParameterNumber")
    public String resolve(
        @PathVariable(name = "id") Long id,
        @ModelAttribute("reviewForm") @Valid AdminLockerIssueReportReviewForm form,
        BindingResult bindingResult,
        Principal principal,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return detail(id, model);
        }
        try {
            adminLockerIssueReportService.resolve(id, form.getReviewMemo(), principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "신고를 처리 완료했습니다.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/locker-issue-reports/" + id;
    }

    @PostMapping("/{id}/reject")
    @SuppressWarnings("checkstyle:ParameterNumber")
    public String reject(
        @PathVariable(name = "id") Long id,
        @ModelAttribute("reviewForm") @Valid AdminLockerIssueReportReviewForm form,
        BindingResult bindingResult,
        Principal principal,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return detail(id, model);
        }
        try {
            adminLockerIssueReportService.reject(id, form.getReviewMemo(), principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "신고를 반려 처리했습니다.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/locker-issue-reports/" + id;
    }

    @ModelAttribute("reviewForm")
    public AdminLockerIssueReportReviewForm reviewForm() {
        return new AdminLockerIssueReportReviewForm();
    }
}
