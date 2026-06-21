package com.zimdugo.admin.application.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class AdminDocumentFormResult {

    private final String title;
    private final String type;
    private final String typeDescription;
    private final String imageUrl;
    private final List<ImageResult> images;
    private final List<SectionResult> sections;

    private AdminDocumentFormResult(AdminDocumentTypeResult documentType) {
        this.title = null;
        this.type = documentType.name();
        this.typeDescription = documentType.getDescription();
        this.imageUrl = null;
        this.images = List.of();
        this.sections = List.of();
    }

    private AdminDocumentFormResult(AdminDocumentDetailResult detail) {
        this.title = detail.getTitle();
        this.type = detail.getType().name();
        this.typeDescription = detail.getType().getDescription();
        this.imageUrl = detail.getImageUrl();
        this.images = detail.getImages().stream()
            .map(image -> new ImageResult(image.id(), image.imageUrl()))
            .toList();
        this.sections = detail.getSections().stream()
            .map(section -> new SectionResult(section.getSubtitle(), section.getContent()))
            .toList();
    }

    public static AdminDocumentFormResult newDocument(AdminDocumentTypeResult type) {
        return new AdminDocumentFormResult(type);
    }

    public static AdminDocumentFormResult from(AdminDocumentDetailResult detail) {
        return new AdminDocumentFormResult(detail);
    }

    public record SectionResult(String subtitle, String content) {
    }

    public record ImageResult(Long id, String imageUrl) {
    }
}
