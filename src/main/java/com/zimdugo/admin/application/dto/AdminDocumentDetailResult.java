package com.zimdugo.admin.application.dto;

import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentSection;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class AdminDocumentDetailResult {

    private final Long id;
    private final AdminDocumentTypeResult type;
    private final String title;
    private final String imageUrl;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime appliedAt;
    private final List<SectionResult> sections;

    private AdminDocumentDetailResult(AdminDocument document) {
        this.id = document.getId();
        this.type = AdminDocumentTypeResult.from(document.getType());
        this.title = document.getTitle();
        this.imageUrl = document.getImageUrl();
        this.active = document.isActive();
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();
        this.appliedAt = document.getAppliedAt();
        this.sections = document.getSections().stream()
            .map(SectionResult::from)
            .toList();
    }

    public static AdminDocumentDetailResult from(AdminDocument document) {
        return new AdminDocumentDetailResult(document);
    }

    @Getter
    public static class SectionResult {

        private final Long id;
        private final String subtitle;
        private final String content;

        private SectionResult(Long id, String subtitle, String content) {
            this.id = id;
            this.subtitle = subtitle;
            this.content = content;
        }

        private static SectionResult from(AdminDocumentSection section) {
            return new SectionResult(section.getId(), section.getSubtitle(), section.getContent());
        }
    }
}
