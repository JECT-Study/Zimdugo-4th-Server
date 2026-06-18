package com.zimdugo.admin.application.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class AdminDocumentFormResult {

    private final String title;
    private final String type;
    private final String typeDescription;
    private final String imageUrl;
    private final List<SectionResult> sections;

    private AdminDocumentFormResult(
        String title,
        String type,
        String typeDescription,
        String imageUrl,
        List<SectionResult> sections
    ) {
        this.title = title;
        this.type = type;
        this.typeDescription = typeDescription;
        this.imageUrl = imageUrl;
        this.sections = sections;
    }

    public static AdminDocumentFormResult newDocument(AdminDocumentTypeResult type) {
        return new AdminDocumentFormResult(null, type.name(), type.getDescription(), null, List.of());
    }

    public static AdminDocumentFormResult from(AdminDocumentDetailResult detail) {
        return new AdminDocumentFormResult(
            detail.getTitle(),
            detail.getType().name(),
            detail.getType().getDescription(),
            detail.getImageUrl(),
            detail.getSections().stream()
                .map(section -> new SectionResult(section.getSubtitle(), section.getContent()))
                .toList()
        );
    }

    public record SectionResult(String subtitle, String content) {
    }
}
