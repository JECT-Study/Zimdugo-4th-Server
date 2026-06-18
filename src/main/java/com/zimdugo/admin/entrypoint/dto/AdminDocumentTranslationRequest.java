package com.zimdugo.admin.entrypoint.dto;

import com.zimdugo.admin.application.dto.AdminDocumentTranslationCommand;
import jakarta.validation.Valid;
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
public class AdminDocumentTranslationRequest {

    @NotBlank
    private String language;

    @NotBlank
    private String title;

    @Valid
    private List<SectionTranslationRequest> sections = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SectionTranslationRequest {

        @NotNull
        private Long sectionId;

        private String subtitle;

        @NotBlank
        private String content;
    }

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
}
