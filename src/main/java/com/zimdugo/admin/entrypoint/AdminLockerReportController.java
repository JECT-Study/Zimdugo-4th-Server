package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.entrypoint.dto.AdminLockerReportApprovalForm;
import com.zimdugo.admin.entrypoint.dto.AdminLockerReportTranslationsForm;
import com.zimdugo.admin.report.AdminLockerReportReviewService;
import com.zimdugo.admin.report.dto.AdminLockerReportReviewPageResult;
import com.zimdugo.admin.translation.AdminLockerReportTranslationService;
import com.zimdugo.admin.translation.dto.AdminLockerReportTranslationPageResult;
import com.zimdugo.admin.translation.dto.AdminTranslationDraftResult;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/locker-reports")
@RequiredArgsConstructor
public class AdminLockerReportController {

    private final AdminLockerReportTranslationService translationService;
    private final AdminLockerReportReviewService reviewService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reports", translationService.getRecentReports());
        model.addAttribute("activeMenu", "locker-reports");
        return "admin/locker-report-list";
    }

    @GetMapping("/{id}")
    public String review(@PathVariable(name = "id") Long id, Model model) {
        AdminLockerReportReviewPageResult page = addReviewPageModel(id, model);
        if (!model.containsAttribute("approvalForm")) {
            model.addAttribute("approvalForm", AdminLockerReportApprovalForm.from(page.report()));
        }
        return "admin/locker-report-review";
    }

    @PostMapping("/{id}/approve")
    @SuppressWarnings("checkstyle:ParameterNumber")
    public String approve(
        @PathVariable(name = "id") Long id,
        @ModelAttribute("approvalForm") @Valid AdminLockerReportApprovalForm form,
        BindingResult bindingResult,
        Principal principal,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addReviewPageModel(id, model);
            return "admin/locker-report-review";
        }
        try {
            reviewService.approve(id, form.toCommand(), principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "제보를 장소와 보관함으로 등록했습니다. 번역 검수가 필요합니다.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/locker-reports/" + id;
    }

    @PostMapping("/{id}/reject")
    public String reject(
        @PathVariable(name = "id") Long id,
        @RequestParam(name = "rejectionMemo") String rejectionMemo,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        try {
            reviewService.reject(id, rejectionMemo, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "제보를 거절했습니다.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/locker-reports/" + id;
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

    @PostMapping("/{id}/translations/draft/{language}")
    @ResponseBody
    public ResponseEntity<RestResponse<AdminTranslationDraftResult>> generateLanguageDraft(
        @PathVariable(name = "id") Long id,
        @PathVariable(name = "language") String language
    ) {
        SupportedLanguage target = SupportedLanguage.fromTag(language)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LANGUAGE_TAG));
        return ResponseEntity.ok(RestResponse.of(
            SuccessCode.OK,
            translationService.generateDraft(id, target)
        ));
    }

    @PostMapping("/{id}/translations/all")
    public String saveTranslations(
        @PathVariable(name = "id") Long id,
        @Valid @ModelAttribute("translationsForm") AdminLockerReportTranslationsForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("translationsForm", form);
            addTranslationPageModel(id, model);
            return "admin/locker-report-translations";
        }
        try {
            translationService.saveTranslations(id, form);
            redirectAttributes.addFlashAttribute(
                "successMessage",
                "번역을 저장했습니다. 최종 승인이 필요합니다."
            );
            return "redirect:/admin/locker-reports/" + id;
        } catch (BusinessException exception) {
            model.addAttribute("saveError", exception.getMessage());
            model.addAttribute("translationsForm", form);
            addTranslationPageModel(id, model);
            return "admin/locker-report-translations";
        }
    }

    @PostMapping("/{id}/approve-translations")
    public String completeApproval(
        @PathVariable(name = "id") Long id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            translationService.completeApproval(id);
            redirectAttributes.addFlashAttribute("successMessage", "제보를 최종 승인했습니다.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/locker-reports/" + id;
    }

    private void addTranslationPageModel(Long id, Model model) {
        AdminLockerReportTranslationPageResult page = translationService.getTranslationPage(id);
        model.addAttribute("page", page);
        if (!model.containsAttribute("translationsForm")) {
            model.addAttribute("translationsForm", AdminLockerReportTranslationsForm.from(page));
        }
        model.addAttribute("activeMenu", "locker-reports");
    }

    private AdminLockerReportReviewPageResult addReviewPageModel(Long id, Model model) {
        AdminLockerReportReviewPageResult page = reviewService.getReviewPage(id);
        model.addAttribute("page", page);
        model.addAttribute("activeMenu", "locker-reports");
        return page;
    }
}
