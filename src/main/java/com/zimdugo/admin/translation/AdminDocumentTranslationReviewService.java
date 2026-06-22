package com.zimdugo.admin.translation;

import com.zimdugo.admin.application.AdminDocumentService;
import com.zimdugo.admin.application.dto.AdminDocumentTranslationCommand;
import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.entrypoint.dto.AdminDocumentTranslationForm;
import com.zimdugo.admin.entrypoint.dto.AdminDocumentTranslationsForm;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationDraftResult;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationReviewPageResult;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationSource;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.ExternalApiException;
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
        return draftGenerator.generate(translationSource(documentId));
    }

    public AdminDocumentTranslationDraftResult generateDraft(
        Long documentId,
        SupportedLanguage language
    ) {
        AdminDocumentTranslationDraftResult draft = draftGenerator.generate(
            translationSource(documentId),
            language
        );
        if (draft.translationFor(language.languageTag()) == null) {
            throw new ExternalApiException("요청한 언어의 번역 응답이 없습니다.");
        }
        return draft;
    }

    private AdminDocumentTranslationSource translationSource(Long documentId) {
        AdminDocument document = adminDocumentService.getById(documentId);
        return AdminDocumentTranslationSource.from(document);
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
