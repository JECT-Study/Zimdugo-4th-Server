package com.zimdugo.admin.entrypoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimdugo.admin.translation.AdminDocumentTranslationReviewService;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationDraftResult;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.response.RestResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AdminDocumentTranslationReviewControllerTest {

    private AdminDocumentTranslationReviewService translationReviewService;
    private AdminDocumentTranslationReviewController controller;

    @BeforeEach
    void setUp() {
        translationReviewService = mock(AdminDocumentTranslationReviewService.class);
        controller = new AdminDocumentTranslationReviewController(translationReviewService);
    }

    @Test
    void generatesDraftForPathLanguageAsJson() {
        AdminDocumentTranslationDraftResult draft = japaneseDraft();
        when(translationReviewService.generateDraft(1L, SupportedLanguage.JAPANESE))
            .thenReturn(draft);

        ResponseEntity<RestResponse<AdminDocumentTranslationDraftResult>> response =
            controller.generateLanguageDraft(1L, "ja");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isSameAs(draft);
        verify(translationReviewService).generateDraft(1L, SupportedLanguage.JAPANESE);
    }

    private AdminDocumentTranslationDraftResult japaneseDraft() {
        return new AdminDocumentTranslationDraftResult(List.of(
            new AdminDocumentTranslationDraftResult.Translation(
                "ja",
                "サービス点検のお知らせ",
                List.of()
            )
        ));
    }
}
