package com.zimdugo.admin.ui.dto;

import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentSection;
import com.zimdugo.admin.domain.DocumentType;
import com.zimdugo.common.i18n.SupportedLanguage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClientDocumentResponse {

    private Long id;
    private DocumentType type;
    private String title;
    private String imageUrl;
    private LocalDateTime appliedAt;
    private List<SectionResponse> sections = new ArrayList<>();

    public ClientDocumentResponse(AdminDocument document) {
        this(document, SupportedLanguage.KOREAN);
    }

    public ClientDocumentResponse(AdminDocument document, SupportedLanguage requestedLanguage) {
        this.id = document.getId();
        this.type = document.getType();
        this.title = document.localizedTitle(requestedLanguage);
        this.imageUrl = document.getImageUrl();
        this.appliedAt = document.getAppliedAt();
        if (document.getSections() != null) {
            this.sections = document.getSections().stream()
                .map(section -> new SectionResponse(section, requestedLanguage))
                .collect(Collectors.toList());
        }
    }

    @Getter
    @NoArgsConstructor
    public static class SectionResponse {
        private String subtitle;
        private String content;

        public SectionResponse(AdminDocumentSection section) {
            this(section, SupportedLanguage.KOREAN);
        }

        public SectionResponse(AdminDocumentSection section, SupportedLanguage requestedLanguage) {
            this.subtitle = section.localizedSubtitle(requestedLanguage);
            this.content = section.localizedContent(requestedLanguage);
        }
    }
}
