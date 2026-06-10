package com.zimdugo.admin.ui;

import com.zimdugo.admin.application.AdminDocumentService;
import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.DocumentType;
import com.zimdugo.admin.ui.dto.AdminDocumentForm;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/admin/documents")
@RequiredArgsConstructor
public class AdminDocumentController {

    private final AdminDocumentService adminDocumentService;

    @GetMapping
    public String list(@RequestParam(name = "type", defaultValue = "NOTICE") DocumentType type, Model model) {
        List<AdminDocument> documents = adminDocumentService.getDocumentsByType(type);
        model.addAttribute("documents", documents);
        model.addAttribute("currentType", type);
        model.addAttribute("activeMenu", type.name().toLowerCase());
        return "admin/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model) {
        AdminDocument document = adminDocumentService.getById(id);
        model.addAttribute("document", document);
        model.addAttribute("activeMenu", document.getType().name().toLowerCase());
        return "admin/detail";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(name = "type", defaultValue = "NOTICE") DocumentType type, Model model) {
        AdminDocumentForm form = new AdminDocumentForm();
        form.setType(type);
        
        // 폼 바인딩을 원활하게 하기 위해 기본적으로 빈 섹션 하나를 포함해둔다.
        form.getSections().add(new AdminDocumentForm.SectionForm());
        
        model.addAttribute("form", form);
        model.addAttribute("isEdit", false);
        model.addAttribute("activeMenu", type.name().toLowerCase());
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
            model.addAttribute("activeMenu", form.getType().name().toLowerCase());
            return "admin/form";
        }
        
        AdminDocument created = adminDocumentService.createDocument(form);
        return "redirect:/admin/documents?type=" + created.getType().name();
    }

    @GetMapping("/{id}/edit")
    public String updateForm(@PathVariable(name = "id") Long id, Model model) {
        AdminDocument document = adminDocumentService.getById(id);
        AdminDocumentForm form = AdminDocumentForm.fromEntity(document);
        
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
            model.addAttribute("activeMenu", form.getType().name().toLowerCase());
            return "admin/form";
        }
        
        AdminDocument updated = adminDocumentService.updateDocument(id, form);
        return "redirect:/admin/documents/" + updated.getId();
    }

    @PostMapping("/{id}/delete")
    public String delete(
        @PathVariable(name = "id") Long id,
        @RequestParam(name = "type") DocumentType type
    ) {
        adminDocumentService.deleteDocument(id);
        return "redirect:/admin/documents?type=" + type.name();
    }

    @PostMapping("/{id}/toggle-active")
    @ResponseBody
    public ResponseEntity<Void> toggleActive(@PathVariable(name = "id") Long id) {
        adminDocumentService.toggleActive(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/toggle-active/detail")
    public String toggleActiveForDetail(@PathVariable(name = "id") Long id) {
        adminDocumentService.toggleActive(id);
        return "redirect:/admin/documents/" + id;
    }

    @PostMapping("/reorder")
    @ResponseBody
    public ResponseEntity<Void> reorder(@RequestBody List<Long> documentIds) {
        adminDocumentService.reorderDocuments(documentIds);
        return ResponseEntity.ok().build();
    }
}
