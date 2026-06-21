package com.zimdugo.admin.application.dto;

import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentSection;
import com.zimdugo.common.i18n.SupportedLanguage;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class ClientDocumentResult {

    private final Long id;
    private final String type;
    private final String title;
    private final String imageUrl;
    private final List<String> imageUrls;
    private final LocalDateTime appliedAt;
    private final List<SectionResult> sections;

    private ClientDocumentResult(AdminDocument document, SupportedLanguage requestedLanguage) {
        this.id = document.getId();
        this.type = document.getType().name();
        this.title = document.localizedTitle(requestedLanguage);
        this.imageUrl = document.getImageUrl();
        this.imageUrls = document.getImageUrls();
        this.appliedAt = document.getAppliedAt();
        this.sections = document.getSections().stream()
            .map(section -> SectionResult.from(section, requestedLanguage))
            .toList();
    }

    public static ClientDocumentResult from(AdminDocument document, SupportedLanguage requestedLanguage) {
        return new ClientDocumentResult(document, requestedLanguage);
    }

    @Getter
    public static class SectionResult {

        private final String subtitle;
        private final String content;

        private SectionResult(String subtitle, String content) {
            this.subtitle = subtitle;
            this.content = content;
        }

        private static SectionResult from(AdminDocumentSection section, SupportedLanguage requestedLanguage) {
            return new SectionResult(
                section.localizedSubtitle(requestedLanguage),
                section.localizedContent(requestedLanguage)
            );
        }
    }
}
