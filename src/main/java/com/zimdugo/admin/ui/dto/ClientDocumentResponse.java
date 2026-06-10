package com.zimdugo.admin.ui.dto;

import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentSection;
import com.zimdugo.admin.domain.DocumentType;
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
    private LocalDateTime appliedAt;
    private List<SectionResponse> sections = new ArrayList<>();

    public ClientDocumentResponse(AdminDocument document) {
        this.id = document.getId();
        this.type = document.getType();
        this.title = document.getTitle();
        this.appliedAt = document.getAppliedAt();
        if (document.getSections() != null) {
            this.sections = document.getSections().stream()
                .map(SectionResponse::new)
                .collect(Collectors.toList());
        }
    }

    @Getter
    @NoArgsConstructor
    public static class SectionResponse {
        private String subtitle;
        private String content;
        private int listOrder;

        public SectionResponse(AdminDocumentSection section) {
            this.subtitle = section.getSubtitle();
            this.content = section.getContent();
            this.listOrder = section.getListOrder();
        }
    }
}
