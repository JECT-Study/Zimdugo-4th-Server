package com.zimdugo.admin.translation;

import com.zimdugo.admin.application.AdminDocumentService;
import com.zimdugo.admin.application.dto.AdminDocumentTranslationCommand;
import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.entrypoint.dto.AdminDocumentTranslationForm;
import com.zimdugo.admin.entrypoint.dto.AdminDocumentTranslationsForm;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationDraftResult;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationReviewPageResult;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDocumentTranslationReviewService {

    private final AdminDocumentService adminDocumentService;
    private final DocumentTranslationDraftGenerator draftGenerator;

    public AdminDocumentTranslationReviewPageResult getReviewPage(Long documentId) {
        return AdminDocumentTranslationReviewPageResult.from(adminDocumentService.getById(documentId));
    }

    public AdminDocumentTranslationDraftResult generateDraft(Long documentId) {
        AdminDocument document = adminDocumentService.getById(documentId);
        return draftGenerator.generate(AdminDocumentTranslationSource.from(document));
    }

    @Transactional
    public void saveTranslation(Long documentId, AdminDocumentTranslationForm form) {
        adminDocumentService.putTranslation(documentId, form.toCommand());
    }

    @Transactional
    public void saveTranslations(Long documentId, AdminDocumentTranslationsForm form) {
        for (AdminDocumentTranslationCommand command : form.toCommands()) {
            adminDocumentService.putTranslation(documentId, command);
        }
    }
}
