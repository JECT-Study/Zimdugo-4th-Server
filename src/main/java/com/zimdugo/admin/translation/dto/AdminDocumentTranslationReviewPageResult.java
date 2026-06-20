package com.zimdugo.admin.translation.dto;

import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentSection;
import com.zimdugo.admin.domain.AdminDocumentSectionTranslation;
import com.zimdugo.admin.domain.AdminDocumentTranslation;
import com.zimdugo.common.i18n.SupportedLanguage;
import java.util.List;

public record AdminDocumentTranslationReviewPageResult(
    Document document,
    List<LanguageReview> languages
) {
    public record Document(
        Long id,
        String type,
        String title,
        boolean active,
        List<Section> sections
    ) {
    }

    public record Section(
        Long id,
        String subtitle,
        String content
    ) {
    }

    public record LanguageReview(
        SupportedLanguage language,
        String title,
        boolean complete,
        List<SectionTranslation> sections
    ) {
    }

    public record SectionTranslation(
        Long sectionId,
        String sourceSubtitle,
        String sourceContent,
        String subtitle,
        String content
    ) {
    }

    public static AdminDocumentTranslationReviewPageResult from(AdminDocument document) {
        Document resultDocument = new Document(
            document.getId(),
            document.getType().name(),
            document.getTitle(),
            document.isActive(),
            document.getSections().stream()
                .map(section -> new Section(section.getId(), section.getSubtitle(), section.getContent()))
                .toList()
        );

        return new AdminDocumentTranslationReviewPageResult(
            resultDocument,
            SupportedLanguage.all().stream()
                .map(language -> languageReview(document, language))
                .toList()
        );
    }

    private static LanguageReview languageReview(AdminDocument document, SupportedLanguage language) {
        String languageTag = language.languageTag();
        String title = document.getTranslations().stream()
            .filter(translation -> translation.getLanguageCode().equals(languageTag))
            .map(AdminDocumentTranslation::getTitle)
            .findFirst()
            .orElse("");

        return new LanguageReview(
            language,
            title,
            document.hasCompleteTranslation(languageTag),
            document.getSections().stream()
                .map(section -> sectionTranslation(section, languageTag))
                .toList()
        );
    }

    private static SectionTranslation sectionTranslation(AdminDocumentSection section, String languageTag) {
        AdminDocumentSectionTranslation translation = section.getTranslations().stream()
            .filter(item -> item.getLanguageCode().equals(languageTag))
            .findFirst()
            .orElse(null);

        return new SectionTranslation(
            section.getId(),
            section.getSubtitle(),
            section.getContent(),
            translation == null ? "" : translation.getSubtitle(),
            translation == null ? "" : translation.getContent()
        );
    }
}
