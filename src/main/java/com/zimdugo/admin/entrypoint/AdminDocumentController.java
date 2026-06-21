package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.application.AdminDocumentService;
import com.zimdugo.admin.application.AdminDocumentImageWorkflow;
import com.zimdugo.admin.application.AdminNoticeImageProperties;
import com.zimdugo.admin.application.dto.AdminDocumentDetailResult;
import com.zimdugo.admin.application.dto.AdminDocumentSummaryResult;
import com.zimdugo.admin.entrypoint.dto.AdminDocumentForm;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.common.storage.S3StorageProperties;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/documents")
@RequiredArgsConstructor
public class AdminDocumentController {

    private final AdminDocumentService adminDocumentService;
    private final AdminDocumentImageWorkflow documentImageWorkflow;
    private final AdminNoticeImageProperties noticeImageProperties;
    private final S3StorageProperties storageProperties;

    @ModelAttribute
    public void addNoticeImageConfiguration(Model model) {
        model.addAttribute("maxNoticeImagePixels", noticeImageProperties.maxPixelCount());
        model.addAttribute("maxNoticeImageBytes", storageProperties.maxUploadBytes());
    }

    @GetMapping
    public String list(@RequestParam(name = "type", defaultValue = "NOTICE") String type, Model model) {
        List<AdminDocumentSummaryResult> documents = adminDocumentService.getDocumentSummaries(type);
        model.addAttribute("documents", documents);
        model.addAttribute("currentType", adminDocumentService.getDocumentType(type));
        model.addAttribute("activeMenu", type.toLowerCase());
        return "admin/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model) {
        AdminDocumentDetailResult document = adminDocumentService.getDocumentDetail(id);
        model.addAttribute("document", document);
        model.addAttribute("activeMenu", document.getType().name().toLowerCase());
        return "admin/detail";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(name = "type", defaultValue = "NOTICE") String type, Model model) {
        AdminDocumentForm form = AdminDocumentForm.fromResult(adminDocumentService.getNewDocumentForm(type));
        
        // 폼 바인딩을 원활하게 하기 위해 기본적으로 빈 섹션 하나를 포함해둔다.
        form.getSections().add(new AdminDocumentForm.SectionForm());
        
        model.addAttribute("form", form);
        model.addAttribute("isEdit", false);
        model.addAttribute("activeMenu", type.toLowerCase());
        return "admin/form";
    }

    @PostMapping
    public String create(
        @ModelAttribute("form") @Valid AdminDocumentForm form,
        BindingResult bindingResult,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("activeMenu", form.getType().toLowerCase());
            return "admin/form";
        }
        
        AdminDocumentDetailResult created;
        try {
            created = documentImageWorkflow.create(form.toCommand(), form.getImageFiles(), form.getImageOrder());
        } catch (BusinessException exception) {
            bindingResult.rejectValue("imageOrder", exception.getErrorCode().getCode(), exception.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeMenu", form.getType().toLowerCase());
            return "admin/form";
        }
        return "redirect:/admin/documents?type=" + created.getType().name();
    }

    @GetMapping("/{id}/edit")
    public String updateForm(@PathVariable(name = "id") Long id, Model model) {
        AdminDocumentDetailResult document = adminDocumentService.getDocumentDetail(id);
        AdminDocumentForm form = AdminDocumentForm.fromResult(adminDocumentService.getDocumentForm(id));
        
        // 만약 기존 섹션이 하나도 없다면 빈 섹션 추가
        if (form.getSections().isEmpty()) {
            form.getSections().add(new AdminDocumentForm.SectionForm());
        }
        
        model.addAttribute("form", form);
        model.addAttribute("isEdit", true);
        model.addAttribute("documentId", id);
        model.addAttribute("activeMenu", document.getType().name().toLowerCase());
        return "admin/form";
    }

    @PostMapping("/{id}/edit")
    public String update(
        @PathVariable(name = "id") Long id,
        @ModelAttribute("form") @Valid AdminDocumentForm form,
        BindingResult bindingResult,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("documentId", id);
            model.addAttribute("activeMenu", form.getType().toLowerCase());
            return "admin/form";
        }
        
        AdminDocumentDetailResult updated;
        try {
            updated = documentImageWorkflow.update(id, form.toCommand(), form.getImageFiles(), form.getImageOrder());
        } catch (BusinessException exception) {
            bindingResult.rejectValue("imageOrder", exception.getErrorCode().getCode(), exception.getMessage());
            AdminDocumentForm existingForm = AdminDocumentForm.fromResult(adminDocumentService.getDocumentForm(id));
            form.setImages(existingForm.getImages());
            model.addAttribute("isEdit", true);
            model.addAttribute("documentId", id);
            model.addAttribute("activeMenu", form.getType().toLowerCase());
            return "admin/form";
        }
        return "redirect:/admin/documents/" + updated.getId();
    }

    @PostMapping("/{id}/delete")
    public String delete(
        @PathVariable(name = "id") Long id,
        @RequestParam(name = "type") String type
    ) {
        adminDocumentService.deleteDocument(id);
        return "redirect:/admin/documents?type=" + type;
    }

    @PostMapping("/{id}/toggle-active")
    @ResponseBody
    public ResponseEntity<Void> toggleActive(@PathVariable(name = "id") Long id) {
        adminDocumentService.toggleActive(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/toggle-active/detail")
    public String toggleActiveForDetail(
        @PathVariable(name = "id") Long id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            adminDocumentService.toggleActive(id);
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/documents/" + id;
    }

    @PostMapping("/reorder")
    @ResponseBody
    public ResponseEntity<Void> reorder(@RequestBody List<Long> documentIds) {
        adminDocumentService.reorderDocuments(documentIds);
        return ResponseEntity.ok().build();
    }
}
