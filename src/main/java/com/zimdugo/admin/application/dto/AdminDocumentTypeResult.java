package com.zimdugo.admin.application.dto;

import com.zimdugo.admin.domain.DocumentType;
import lombok.Getter;

@Getter
public class AdminDocumentTypeResult {

    private final String name;
    private final String description;

    private AdminDocumentTypeResult(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String name() {
        return name;
    }

    public static AdminDocumentTypeResult from(DocumentType type) {
        return new AdminDocumentTypeResult(type.name(), type.getDescription());
    }
}
