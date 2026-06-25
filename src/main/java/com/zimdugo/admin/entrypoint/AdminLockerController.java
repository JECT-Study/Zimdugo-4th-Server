package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.application.AdminNoticeImageStorage;
import com.zimdugo.admin.i18n.LockerContentI18nAdminService;
import com.zimdugo.admin.locker.AdminLockerService;
import com.zimdugo.admin.locker.dto.AdminLockerCommand;
import com.zimdugo.admin.locker.dto.AdminLockerForm;
import com.zimdugo.admin.locker.dto.AdminLockerDetailResult;
import com.zimdugo.admin.locker.dto.AdminLockerTranslationForm;
import com.zimdugo.admin.translation.dto.AdminTranslationDraftResult;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/lockers")
@RequiredArgsConstructor
public class AdminLockerController {

    private final AdminLockerService adminLockerService;
    private final LockerContentI18nAdminService i18nAdminService;
    private final AdminNoticeImageStorage imageStorage;

    @ModelAttribute
    public void addOptions(Model model) {
        model.addAttribute("activeMenu", "lockers");
        model.addAttribute("lockerTypes", adminLockerService.getLockerTypeOptions());
        model.addAttribute("indoorOutdoorTypes", adminLockerService.getIndoorOutdoorTypeOptions());
        model.addAttribute("groundLevelTypes", adminLockerService.getGroundLevelTypeOptions());
        model.addAttribute("lockerSizeTypes", adminLockerService.getLockerSizeOptions());
    }

    @GetMapping
    public String list(
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "page", defaultValue = "0") int page,
        Model model
    ) {
        model.addAttribute("lockerPage", adminLockerService.getLockers(keyword, page));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "admin/locker-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model) {
        model.addAttribute("locker", adminLockerService.getLocker(id));
        return "admin/locker-detail";
    }

    @GetMapping("/{id}/edit")
    public String updateForm(@PathVariable(name = "id") Long id, Model model) {
        model.addAttribute("form", AdminLockerForm.from(adminLockerService.getLocker(id)));
        model.addAttribute("lockerId", id);
        return "admin/locker-form";
    }

    @PostMapping("/{id}/edit")
    public String update(
        @PathVariable(name = "id") Long id,
        @ModelAttribute("form") @Valid AdminLockerForm form,
        BindingResult bindingResult,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("lockerId", id);
            return "admin/locker-form";
        }

        try {
            adminLockerService.updateLocker(id, toCommandWithImage(form));
            return "redirect:/admin/lockers/" + id + "/translations";
        } catch (BusinessException exception) {
            bindingResult.reject("adminLocker", exception.getMessage());
            model.addAttribute("lockerId", id);
            return "admin/locker-form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(
        @PathVariable(name = "id") Long id,
        RedirectAttributes redirectAttributes
    ) {
        AdminLockerDetailResult locker = adminLockerService.getLocker(id);
        adminLockerService.deleteLocker(id);
        if (locker.imageUrl() != null) {
            imageStorage.deleteAll(List.of(locker.imageUrl()));
        }
        redirectAttributes.addFlashAttribute("successMessage", "보관함을 삭제했습니다.");
        return "redirect:/admin/lockers";
    }

    @GetMapping("/{id}/translations")
    public String translations(@PathVariable(name = "id") Long id, Model model) {
        model.addAttribute("locker", adminLockerService.getLocker(id));
        model.addAttribute("translationsForm", AdminLockerTranslationForm.from(i18nAdminService.getLocker(id)));
        return "admin/locker-translations";
    }

    @PostMapping("/{id}/translations/draft")
    public String generateDraft(
        @PathVariable(name = "id") Long id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            redirectAttributes.addFlashAttribute("draft", adminLockerService.generateDraft(id));
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("draftError", exception.getMessage());
        }
        return "redirect:/admin/lockers/" + id + "/translations";
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
            adminLockerService.generateDraft(id, target)
        ));
    }

    @PostMapping("/{id}/translations")
    public String saveTranslations(
        @PathVariable(name = "id") Long id,
        @ModelAttribute("translationsForm") @Valid AdminLockerTranslationForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("locker", adminLockerService.getLocker(id));
            return "admin/locker-translations";
        }
        i18nAdminService.replaceLocker(id, form.toRequest());
        redirectAttributes.addFlashAttribute("successMessage", "번역을 저장했습니다. 승인하면 공개됩니다.");
        return "redirect:/admin/lockers/" + id + "/translations";
    }

    @PostMapping("/{id}/approve")
    public String approve(
        @PathVariable(name = "id") Long id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            adminLockerService.approveLocker(id);
            redirectAttributes.addFlashAttribute("successMessage", "보관함을 승인하고 공개했습니다.");
            return "redirect:/admin/lockers";
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("saveError", exception.getMessage());
            return "redirect:/admin/lockers/" + id + "/translations";
        }
    }

    private AdminLockerCommand toCommandWithImage(AdminLockerForm form) {
        if (form.getImageFile() == null || form.getImageFile().isEmpty()) {
            return form.toCommand();
        }
        String imageUrl = imageStorage.uploadAll(List.of(form.getImageFile())).getFirst();
        form.setImageUrl(imageUrl);
        return form.toCommand();
    }
}
