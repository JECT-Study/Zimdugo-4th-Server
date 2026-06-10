package com.zimdugo.admin.ui.dto;

import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentSection;
import com.zimdugo.admin.domain.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminDocumentForm {

    @NotBlank(message = "대표 제목은 필수 입력 항목입니다.")
    private String title;

    @NotNull(message = "문서 타입은 필수 항목입니다.")
    private DocumentType type;

    private List<SectionForm> sections = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SectionForm {
        private String subtitle;

        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        private String content;
    }

    public static AdminDocumentForm fromEntity(AdminDocument document) {
        AdminDocumentForm form = new AdminDocumentForm();
        form.setTitle(document.getTitle());
        form.setType(document.getType());
        
        List<SectionForm> sectionForms = new ArrayList<>();
        if (document.getSections() != null) {
            for (AdminDocumentSection sec : document.getSections()) {
                SectionForm secForm = new SectionForm();
                secForm.setSubtitle(sec.getSubtitle());
                secForm.setContent(sec.getContent());
                sectionForms.add(secForm);
            }
        }
        form.setSections(sectionForms);
        return form;
    }

    public AdminDocument toEntity() {
        List<AdminDocumentSection> sectionEntities = new ArrayList<>();
        if (this.sections != null) {
            for (int i = 0; i < this.sections.size(); i++) {
                SectionForm sec = this.sections.get(i);
                // 내용이 비어있으면 저장에서 누락 처리할 수 있으나, 기본 밸리데이션 검사 권장
                if (sec.getContent() != null && !sec.getContent().isBlank()) {
                    sectionEntities.add(AdminDocumentSection.builder()
                        .subtitle(sec.getSubtitle())
                        .content(sec.getContent())
                        .listOrder(i)
                        .build());
                }
            }
        }
        return AdminDocument.builder()
            .title(this.title)
            .type(this.type)
            .sections(sectionEntities)
            .build();
    }
}
