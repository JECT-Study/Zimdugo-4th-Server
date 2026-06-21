package com.zimdugo.admin.application.dto;

import java.util.ArrayList;
import java.util.List;

public record AdminDocumentCommand(
    String title,
    String type,
    String imageUrl,
    List<String> imageUrls,
    List<SectionCommand> sections
) {

    public AdminDocumentCommand(String title, String type, String imageUrl, List<SectionCommand> sections) {
        this(title, type, imageUrl, imageUrl == null || imageUrl.isBlank() ? List.of() : List.of(imageUrl), sections);
    }

    public List<SectionCommand> sectionsOrEmpty() {
        return sections == null ? List.of() : sections;
    }

    public List<String> imageUrlsOrEmpty() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls;
        }
        return imageUrl == null || imageUrl.isBlank() ? List.of() : List.of(imageUrl);
    }

    public AdminDocumentCommand withImageUrls(List<String> newImageUrls) {
        String firstImageUrl = newImageUrls == null || newImageUrls.isEmpty() ? null : newImageUrls.getFirst();
        return new AdminDocumentCommand(title, type, firstImageUrl, newImageUrls, sections);
    }

    public record SectionCommand(String subtitle, String content) {
    }

    public static AdminDocumentCommand empty(String type) {
        return new AdminDocumentCommand(null, type, null, List.of(), new ArrayList<>());
    }
}
