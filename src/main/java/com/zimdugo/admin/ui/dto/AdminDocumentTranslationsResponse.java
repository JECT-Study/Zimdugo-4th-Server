package com.zimdugo.admin.ui.dto;

import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentSection;
import com.zimdugo.admin.domain.AdminDocumentSectionTranslation;
import com.zimdugo.admin.domain.AdminDocumentTranslation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import lombok.Getter;

@Getter
public class AdminDocumentTranslationsResponse {

    private final Long documentId;
    private final List<TranslationResponse> translations;

    private AdminDocumentTranslationsResponse(Long documentId, List<TranslationResponse> translations) {
        this.documentId = documentId;
        this.translations = translations;
    }

    public static AdminDocumentTranslationsResponse from(AdminDocument document) {
        Set<String> languages = new TreeSet<>();
        document.getTranslations().stream()
            .map(AdminDocumentTranslation::getLanguageCode)
            .forEach(languages::add);
        document.getSections().stream()
            .flatMap(section -> section.getTranslations().stream())
            .map(AdminDocumentSectionTranslation::getLanguageCode)
            .forEach(languages::add);

        List<TranslationResponse> responses = languages.stream()
            .map(language -> TranslationResponse.from(document, language))
            .toList();
        return new AdminDocumentTranslationsResponse(document.getId(), responses);
    }

    @Getter
    public static class TranslationResponse {

        private final String language;
        private final String title;
        private final List<SectionTranslationResponse> sections;

        private TranslationResponse(String language, String title, List<SectionTranslationResponse> sections) {
            this.language = language;
            this.title = title;
            this.sections = sections;
        }

        private static TranslationResponse from(AdminDocument document, String language) {
            String translatedTitle = document.getTranslations().stream()
                .filter(translation -> translation.getLanguageCode().equals(language))
                .map(AdminDocumentTranslation::getTitle)
                .findFirst()
                .orElse(null);

            List<SectionTranslationResponse> sectionResponses = new ArrayList<>();
            for (AdminDocumentSection section : document.getSections()) {
                section.getTranslations().stream()
                    .filter(translation -> translation.getLanguageCode().equals(language))
                    .findFirst()
                    .ifPresent(translation -> sectionResponses.add(
                        SectionTranslationResponse.from(section, translation)
                    ));
            }
            return new TranslationResponse(language, translatedTitle, sectionResponses);
        }
    }

    @Getter
    public static class SectionTranslationResponse {

        private final Long sectionId;
        private final String subtitle;
        private final String content;

        private SectionTranslationResponse(Long sectionId, String subtitle, String content) {
            this.sectionId = sectionId;
            this.subtitle = subtitle;
            this.content = content;
        }

        private static SectionTranslationResponse from(
            AdminDocumentSection section,
            AdminDocumentSectionTranslation translation
        ) {
            return new SectionTranslationResponse(
                section.getId(),
                translation.getSubtitle(),
                translation.getContent()
            );
        }
    }
}
