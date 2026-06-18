package com.zimdugo.admin.application.dto;

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
public class AdminDocumentTranslationsResult {

    private final Long documentId;
    private final List<TranslationResult> translations;

    private AdminDocumentTranslationsResult(Long documentId, List<TranslationResult> translations) {
        this.documentId = documentId;
        this.translations = translations;
    }

    public static AdminDocumentTranslationsResult from(AdminDocument document) {
        Set<String> languages = new TreeSet<>();
        document.getTranslations().stream()
            .map(AdminDocumentTranslation::getLanguageCode)
            .forEach(languages::add);
        document.getSections().stream()
            .flatMap(section -> section.getTranslations().stream())
            .map(AdminDocumentSectionTranslation::getLanguageCode)
            .forEach(languages::add);

        List<TranslationResult> responses = languages.stream()
            .map(language -> TranslationResult.from(document, language))
            .toList();
        return new AdminDocumentTranslationsResult(document.getId(), responses);
    }

    @Getter
    public static class TranslationResult {

        private final String language;
        private final String title;
        private final List<SectionTranslationResult> sections;

        private TranslationResult(String language, String title, List<SectionTranslationResult> sections) {
            this.language = language;
            this.title = title;
            this.sections = sections;
        }

        private static TranslationResult from(AdminDocument document, String language) {
            String translatedTitle = document.getTranslations().stream()
                .filter(translation -> translation.getLanguageCode().equals(language))
                .map(AdminDocumentTranslation::getTitle)
                .findFirst()
                .orElse(null);

            List<SectionTranslationResult> sectionResponses = new ArrayList<>();
            for (AdminDocumentSection section : document.getSections()) {
                section.getTranslations().stream()
                    .filter(translation -> translation.getLanguageCode().equals(language))
                    .findFirst()
                    .ifPresent(translation -> sectionResponses.add(
                        SectionTranslationResult.from(section, translation)
                    ));
            }
            return new TranslationResult(language, translatedTitle, sectionResponses);
        }
    }

    @Getter
    public static class SectionTranslationResult {

        private final Long sectionId;
        private final String subtitle;
        private final String content;

        private SectionTranslationResult(Long sectionId, String subtitle, String content) {
            this.sectionId = sectionId;
            this.subtitle = subtitle;
            this.content = content;
        }

        private static SectionTranslationResult from(
            AdminDocumentSection section,
            AdminDocumentSectionTranslation translation
        ) {
            return new SectionTranslationResult(
                section.getId(),
                translation.getSubtitle(),
                translation.getContent()
            );
        }
    }
}
