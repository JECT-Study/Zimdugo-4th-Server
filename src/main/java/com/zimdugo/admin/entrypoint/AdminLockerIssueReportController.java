package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.entrypoint.dto.AdminLockerIssueReportReviewForm;
import com.zimdugo.admin.issue.AdminLockerIssueReportService;
import com.zimdugo.admin.issue.dto.AdminLockerIssueReportDetailResult;
import com.zimdugo.admin.issue.dto.AdminLockerIssueReportReviewCommand;
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

    private static final String REVIEW_FORM_ATTRIBUTE = "reviewForm";
    private static final String REVIEW_FORM_BINDING_RESULT_PREFIX =
        "org.springframework.validation.BindingResult.";

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
        AdminLockerIssueReportDetailResult report = adminLockerIssueReportService.getReport(id);
        model.addAttribute("report", report);
        if (!model.containsAttribute(REVIEW_FORM_ATTRIBUTE)) {
            model.addAttribute(REVIEW_FORM_ATTRIBUTE, createDefaultReviewForm(report));
        }
        model.addAttribute("activeMenu", "locker-issue-reports");
        return "admin/locker-issue-report-detail";
    }

    @PostMapping("/{id}/resolve")
    public String resolve(
        @PathVariable(name = "id") Long id,
        @ModelAttribute(REVIEW_FORM_ATTRIBUTE) @Valid AdminLockerIssueReportReviewForm form,
        BindingResult bindingResult,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return redirectToDetailWithValidation(id, form, bindingResult, redirectAttributes);
        }
        try {
            adminLockerIssueReportService.resolve(toReviewCommand(id, form, principal));
            redirectAttributes.addFlashAttribute("successMessage", "신고 상태를 처리 완료로 저장했습니다.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return redirectToDetail(id);
    }

    @PostMapping("/{id}/reject")
    public String reject(
        @PathVariable(name = "id") Long id,
        @ModelAttribute(REVIEW_FORM_ATTRIBUTE) @Valid AdminLockerIssueReportReviewForm form,
        BindingResult bindingResult,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return redirectToDetailWithValidation(id, form, bindingResult, redirectAttributes);
        }
        try {
            adminLockerIssueReportService.reject(toReviewCommand(id, form, principal));
            redirectAttributes.addFlashAttribute("successMessage", "신고 상태를 반려로 저장했습니다.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return redirectToDetail(id);
    }

    @ModelAttribute(REVIEW_FORM_ATTRIBUTE)
    public AdminLockerIssueReportReviewForm reviewForm() {
        return new AdminLockerIssueReportReviewForm();
    }

    private AdminLockerIssueReportReviewForm createDefaultReviewForm(AdminLockerIssueReportDetailResult report) {
        AdminLockerIssueReportReviewForm form = new AdminLockerIssueReportReviewForm();
        if (report.reviewMemo() != null) {
            form.setReviewMemo(report.reviewMemo());
        }
        return form;
    }

    private String redirectToDetailWithValidation(
        Long id,
        AdminLockerIssueReportReviewForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute(REVIEW_FORM_ATTRIBUTE, form);
        redirectAttributes.addFlashAttribute(
            REVIEW_FORM_BINDING_RESULT_PREFIX + REVIEW_FORM_ATTRIBUTE,
            bindingResult
        );
        return redirectToDetail(id);
    }

    private AdminLockerIssueReportReviewCommand toReviewCommand(
        Long id,
        AdminLockerIssueReportReviewForm form,
        Principal principal
    ) {
        return new AdminLockerIssueReportReviewCommand(id, form.getReviewMemo(), principal.getName());
    }

    private String redirectToDetail(Long id) {
        return "redirect:/admin/locker-issue-reports/" + id;
    }
}
