package com.zimdugo.admin.application.dto;

import java.util.ArrayList;
import java.util.List;

public record AdminDocumentCommand(
    String title,
    String type,
    String imageUrl,
    List<SectionCommand> sections
) {

    public List<SectionCommand> sectionsOrEmpty() {
        return sections == null ? List.of() : sections;
    }

    public record SectionCommand(String subtitle, String content) {
    }

    public static AdminDocumentCommand empty(String type) {
        return new AdminDocumentCommand(null, type, null, new ArrayList<>());
    }
}
