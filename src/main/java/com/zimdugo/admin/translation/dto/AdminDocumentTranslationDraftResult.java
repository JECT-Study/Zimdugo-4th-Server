package com.zimdugo.admin.translation.dto;

import java.util.List;

public record AdminDocumentTranslationDraftResult(
    List<Translation> translations
) {
    public Translation translationFor(String language) {
        if (language == null || translations == null) {
            return null;
        }
        return translations.stream()
            .filter(translation -> language.equals(translation.language()))
            .findFirst()
            .orElse(null);
    }

    public record Translation(
        String language,
        String title,
        List<Section> sections
    ) {
    }

    public record Section(
        Long sectionId,
        String subtitle,
        String content
    ) {
    }
}
