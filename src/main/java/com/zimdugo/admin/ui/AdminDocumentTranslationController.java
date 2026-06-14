package com.zimdugo.admin.ui;

import com.zimdugo.admin.application.AdminDocumentService;
import com.zimdugo.admin.ui.dto.AdminDocumentTranslationRequest;
import com.zimdugo.admin.ui.dto.AdminDocumentTranslationsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/i18n/documents")
@RequiredArgsConstructor
public class AdminDocumentTranslationController {

    private final AdminDocumentService adminDocumentService;

    @GetMapping("/{id}")
    public ResponseEntity<AdminDocumentTranslationsResponse> getTranslations(
        @PathVariable(name = "id") Long id
    ) {
        return ResponseEntity.ok(adminDocumentService.getTranslations(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminDocumentTranslationsResponse> putTranslation(
        @PathVariable(name = "id") Long id,
        @Valid @RequestBody AdminDocumentTranslationRequest request
    ) {
        return ResponseEntity.ok(adminDocumentService.putTranslation(id, request));
    }
}
