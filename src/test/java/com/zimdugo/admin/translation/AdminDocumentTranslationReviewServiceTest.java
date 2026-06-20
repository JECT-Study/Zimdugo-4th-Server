package com.zimdugo.admin.translation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimdugo.admin.application.AdminDocumentService;
import com.zimdugo.admin.application.dto.AdminDocumentTranslationCommand;
import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentSection;
import com.zimdugo.admin.domain.DocumentType;
import com.zimdugo.admin.entrypoint.dto.AdminDocumentTranslationForm;
import com.zimdugo.admin.entrypoint.dto.AdminDocumentTranslationsForm;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationDraftResult;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationReviewPageResult;
import com.zimdugo.admin.translation.dto.AdminDocumentTranslationSource;
import com.zimdugo.common.i18n.SupportedLanguage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminDocumentTranslationReviewServiceTest {

    @Mock
    private AdminDocumentService adminDocumentService;
    @Mock
    private DocumentTranslationDraftGenerator draftGenerator;

    private AdminDocumentTranslationReviewService service;

    @BeforeEach
    void setUp() {
        service = new AdminDocumentTranslationReviewService(adminDocumentService, draftGenerator);
    }

    @Test
    void getsReviewPageForEverySupportedLanguage() {
        AdminDocument document = document();
        addEnglishTranslation(document);
        when(adminDocumentService.getById(1L)).thenReturn(document);

        AdminDocumentTranslationReviewPageResult result = service.getReviewPage(1L);

        assertThat(result.document().title()).isEqualTo("서비스 점검 안내");
        assertThat(result.languages()).hasSize(SupportedLanguage.all().size());
        assertThat(result.languages())
            .filteredOn(language -> language.language() == SupportedLanguage.ENGLISH)
            .singleElement()
            .satisfies(language -> {
                assertThat(language.complete()).isTrue();
                assertThat(language.title()).isEqualTo("Service Maintenance Notice");
            });
    }

    @Test
    void generatesDraftFromDocumentSource() {
        AdminDocument document = document();
        AdminDocumentTranslationDraftResult draft = new AdminDocumentTranslationDraftResult(List.of());
        when(adminDocumentService.getById(1L)).thenReturn(document);
        when(draftGenerator.generate(any())).thenReturn(draft);

        AdminDocumentTranslationDraftResult result = service.generateDraft(1L);

        ArgumentCaptor<AdminDocumentTranslationSource> captor =
            ArgumentCaptor.forClass(AdminDocumentTranslationSource.class);
        verify(draftGenerator).generate(captor.capture());
        assertThat(result).isSameAs(draft);
        assertThat(captor.getValue().title()).isEqualTo("서비스 점검 안내");
        assertThat(captor.getValue().sections()).hasSize(1);
    }

    @Test
    void savesTranslationThroughExistingDocumentService() {
        AdminDocumentTranslationForm form = new AdminDocumentTranslationForm();
        form.setLanguage("en");
        form.setTitle("Service Maintenance Notice");

        AdminDocumentTranslationForm.SectionForm section = new AdminDocumentTranslationForm.SectionForm();
        section.setSectionId(10L);
        section.setSubtitle("Maintenance");
        section.setContent("Service is temporarily unavailable.");
        form.setSections(List.of(section));

        service.saveTranslation(1L, form);

        ArgumentCaptor<AdminDocumentTranslationCommand> captor =
            ArgumentCaptor.forClass(AdminDocumentTranslationCommand.class);
        verify(adminDocumentService).putTranslation(eq(1L), captor.capture());
        assertThat(captor.getValue().language()).isEqualTo("en");
        assertThat(captor.getValue().sections()).hasSize(1);
        assertThat(captor.getValue().sections().getFirst().sectionId()).isEqualTo(10L);
    }

    @Test
    void savesAllTranslationsThroughExistingDocumentService() {
        AdminDocumentTranslationsForm form = new AdminDocumentTranslationsForm();
        form.setTranslations(List.of(
            translationForm("en", "Service Maintenance Notice"),
            translationForm("ja", "サービス点検のお知らせ")
        ));

        service.saveTranslations(1L, form);

        ArgumentCaptor<AdminDocumentTranslationCommand> captor =
            ArgumentCaptor.forClass(AdminDocumentTranslationCommand.class);
        verify(adminDocumentService, times(2)).putTranslation(eq(1L), captor.capture());
        assertThat(captor.getAllValues())
            .extracting(AdminDocumentTranslationCommand::language)
            .containsExactly("en", "ja");
    }

    private AdminDocument document() {
        AdminDocumentSection section = AdminDocumentSection.builder()
            .subtitle("점검")
            .content("서비스 이용이 일시 중단됩니다.")
            .listOrder(0)
            .build();
        return AdminDocument.builder()
            .type(DocumentType.NOTICE)
            .title("서비스 점검 안내")
            .sections(List.of(section))
            .build();
    }

    private void addEnglishTranslation(AdminDocument document) {
        document.upsertTranslation("en", "Service Maintenance Notice");
        document.getSections().getFirst().upsertTranslation(
            "en",
            "Maintenance",
            "Service is temporarily unavailable."
        );
    }

    private AdminDocumentTranslationsForm.TranslationForm translationForm(String language, String title) {
        AdminDocumentTranslationsForm.TranslationForm form = new AdminDocumentTranslationsForm.TranslationForm();
        form.setLanguage(language);
        form.setTitle(title);

        AdminDocumentTranslationForm.SectionForm section = new AdminDocumentTranslationForm.SectionForm();
        section.setSectionId(10L);
        section.setSubtitle("Maintenance");
        section.setContent("Service is temporarily unavailable.");
        form.setSections(List.of(section));

        return form;
    }
}
