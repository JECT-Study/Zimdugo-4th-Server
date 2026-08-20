package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.issue.AdminLockerIssueReportService;
import com.zimdugo.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
        model.addAttribute("activeMenu", "locker-issue-reports");
        return "admin/locker-issue-report-detail";
    }

    @PostMapping("/{id}/resolve")
    public String resolve(
        @PathVariable(name = "id") Long id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            adminLockerIssueReportService.resolve(id);
            redirectAttributes.addFlashAttribute("successMessage", "신고를 처리 완료했습니다.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/locker-issue-reports/" + id;
    }

    @PostMapping("/{id}/reject")
    public String reject(
        @PathVariable(name = "id") Long id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            adminLockerIssueReportService.reject(id);
            redirectAttributes.addFlashAttribute("successMessage", "신고를 반려 처리했습니다.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/locker-issue-reports/" + id;
    }
}
