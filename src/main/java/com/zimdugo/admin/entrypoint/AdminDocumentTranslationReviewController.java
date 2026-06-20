package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.entrypoint.dto.AdminDocumentTranslationForm;
import com.zimdugo.admin.entrypoint.dto.AdminDocumentTranslationsForm;
import com.zimdugo.admin.translation.AdminDocumentTranslationReviewService;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationDraftResult;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationReviewPageResult;
import com.zimdugo.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/documents")
@RequiredArgsConstructor
public class AdminDocumentTranslationReviewController {

    private final AdminDocumentTranslationReviewService translationReviewService;

    @GetMapping("/{id}/translations")
    public String translations(@PathVariable(name = "id") Long id, Model model) {
        addReviewPageModel(id, model);
        return "admin/document-translations";
    }

    @PostMapping("/{id}/translations/draft")
    public String generateDraft(
        @PathVariable(name = "id") Long id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            AdminDocumentTranslationDraftResult draft = translationReviewService.generateDraft(id);
            redirectAttributes.addFlashAttribute("draft", draft);
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("draftError", e.getMessage());
        }
        return "redirect:/admin/documents/" + id + "/translations";
    }

    @PostMapping("/{id}/translations")
    public String saveTranslation(
        @PathVariable(name = "id") Long id,
        @ModelAttribute AdminDocumentTranslationForm form
    ) {
        translationReviewService.saveTranslation(id, form);
        return "redirect:/admin/documents/" + id + "/translations";
    }

    @PostMapping("/{id}/translations/all")
    public String saveTranslations(
        @PathVariable(name = "id") Long id,
        @ModelAttribute AdminDocumentTranslationsForm form,
        RedirectAttributes redirectAttributes
    ) {
        translationReviewService.saveTranslations(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "저장되었습니다.");
        return "redirect:/admin/documents/" + id + "/translations";
    }

    private void addReviewPageModel(Long id, Model model) {
        AdminDocumentTranslationReviewPageResult page = translationReviewService.getReviewPage(id);
        model.addAttribute("page", page);
        model.addAttribute("activeMenu", page.document().type().toLowerCase());
    }
}
