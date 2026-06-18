package com.zimdugo.admin.application.dto;

import com.zimdugo.admin.domain.AdminDocument;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class AdminDocumentSummaryResult {

    private final Long id;
    private final AdminDocumentTypeResult type;
    private final String title;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime appliedAt;

    private AdminDocumentSummaryResult(AdminDocument document) {
        this.id = document.getId();
        this.type = AdminDocumentTypeResult.from(document.getType());
        this.title = document.getTitle();
        this.active = document.isActive();
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();
        this.appliedAt = document.getAppliedAt();
    }

    public static AdminDocumentSummaryResult from(AdminDocument document) {
        return new AdminDocumentSummaryResult(document);
    }
}
