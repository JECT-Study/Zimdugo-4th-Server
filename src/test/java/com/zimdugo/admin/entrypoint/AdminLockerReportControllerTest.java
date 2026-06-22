package com.zimdugo.admin.entrypoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimdugo.admin.entrypoint.dto.AdminLockerReportTranslationsForm;
import com.zimdugo.admin.report.AdminLockerReportReviewService;
import com.zimdugo.admin.translation.AdminLockerReportTranslationService;
import com.zimdugo.admin.translation.dto.AdminLockerReportTranslationPageResult;
import com.zimdugo.admin.translation.dto.AdminTranslationDraftResult;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.response.RestResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class AdminLockerReportControllerTest {

    private AdminLockerReportTranslationService translationService;
    private AdminLockerReportController controller;

    @BeforeEach
    void setUp() {
        translationService = mock(AdminLockerReportTranslationService.class);
        controller = new AdminLockerReportController(
            translationService,
            mock(AdminLockerReportReviewService.class)
        );
    }

    @Test
    void savesTranslationsAndReturnsToReportReview() {
        AdminLockerReportTranslationsForm form = new AdminLockerReportTranslationsForm();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.saveTranslations(
            1L,
            form,
            bindingResult,
            new ConcurrentModel(),
            redirectAttributes
        );

        verify(translationService).saveTranslations(1L, form);
        assertThat(view).isEqualTo("redirect:/admin/locker-reports/1");
        assertThat(redirectAttributes.getFlashAttributes().get("successMessage"))
            .isEqualTo("번역을 저장했습니다. 최종 승인이 필요합니다.");
    }

    @Test
    void initializesTranslationFormFromSavedValues() {
        AdminLockerReportTranslationPageResult page =
            mock(AdminLockerReportTranslationPageResult.class);
        when(page.languages()).thenReturn(List.of());
        when(translationService.getTranslationPage(1L)).thenReturn(page);
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.translations(1L, model);

        assertThat(view).isEqualTo("admin/locker-report-translations");
        assertThat(model.getAttribute("translationsForm"))
            .isInstanceOf(AdminLockerReportTranslationsForm.class);
    }

    @Test
    void completesFinalApprovalAndReturnsToReportReview() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.completeApproval(1L, redirectAttributes);

        verify(translationService).completeApproval(1L);
        assertThat(view).isEqualTo("redirect:/admin/locker-reports/1");
    }

    @Test
    void generatesDraftForPathLanguageAsJson() {
        AdminTranslationDraftResult draft = japaneseDraft();
        when(translationService.generateDraft(1L, SupportedLanguage.JAPANESE))
            .thenReturn(draft);

        ResponseEntity<RestResponse<AdminTranslationDraftResult>> response =
            controller.generateLanguageDraft(1L, "ja");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isSameAs(draft);
        verify(translationService).generateDraft(1L, SupportedLanguage.JAPANESE);
    }

    private AdminTranslationDraftResult japaneseDraft() {
        return new AdminTranslationDraftResult(
            List.of(new AdminTranslationDraftResult.PlaceTranslation(
                SupportedLanguage.JAPANESE,
                "ソウル駅",
                "ソウル特別市中区",
                List.of("ソウル駅")
            )),
            List.of(new AdminTranslationDraftResult.LockerTranslation(
                SupportedLanguage.JAPANESE,
                "ソウル駅ロッカー",
                "ソウル特別市中区",
                "1番出口付近",
                List.of("コインロッカー")
            ))
        );
    }
}
