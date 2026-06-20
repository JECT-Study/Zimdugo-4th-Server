package com.zimdugo.admin.translation.dto;

import com.zimdugo.admin.domain.AdminDocument;
import java.util.List;

public record AdminDocumentTranslationSource(
    Long documentId,
    String type,
    String title,
    List<Section> sections
) {
    public record Section(
        Long sectionId,
        String subtitle,
        String content
    ) {
    }

    public static AdminDocumentTranslationSource from(AdminDocument document) {
        return new AdminDocumentTranslationSource(
            document.getId(),
            document.getType().name(),
            document.getTitle(),
            document.getSections().stream()
                .map(section -> new Section(section.getId(), section.getSubtitle(), section.getContent()))
                .toList()
        );
    }
}
