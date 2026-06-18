package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.application.AdminDocumentService;
import com.zimdugo.admin.application.dto.AdminDocumentTranslationsResult;
import com.zimdugo.admin.entrypoint.dto.AdminDocumentTranslationRequest;
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
    public ResponseEntity<AdminDocumentTranslationsResult> getTranslations(
        @PathVariable(name = "id") Long id
    ) {
        return ResponseEntity.ok(adminDocumentService.getTranslations(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminDocumentTranslationsResult> putTranslation(
        @PathVariable(name = "id") Long id,
        @Valid @RequestBody AdminDocumentTranslationRequest request
    ) {
        return ResponseEntity.ok(adminDocumentService.putTranslation(id, request.toCommand()));
    }
}
