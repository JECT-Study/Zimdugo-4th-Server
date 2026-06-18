package com.zimdugo.admin.application.dto;

import java.util.List;

public record AdminDocumentTranslationCommand(
    String language,
    String title,
    List<SectionTranslationCommand> sections
) {

    public List<SectionTranslationCommand> sectionsOrEmpty() {
        return sections == null ? List.of() : sections;
    }

    public record SectionTranslationCommand(
        Long sectionId,
        String subtitle,
        String content
    ) {
    }
}
