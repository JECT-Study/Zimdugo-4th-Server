package com.zimdugo.admin.entrypoint.dto;

import com.zimdugo.admin.application.dto.AdminDocumentTranslationCommand;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminDocumentTranslationForm {

    private String language;
    private String title;
    private List<SectionForm> sections = new ArrayList<>();

    public AdminDocumentTranslationCommand toCommand() {
        return new AdminDocumentTranslationCommand(
            language,
            title,
            sections == null ? List.of() : sections.stream()
                .map(section -> new AdminDocumentTranslationCommand.SectionTranslationCommand(
                    section.getSectionId(),
                    section.getSubtitle(),
                    section.getContent()
                ))
                .toList()
        );
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SectionForm {

        private Long sectionId;
        private String subtitle;
        private String content;
    }
}
