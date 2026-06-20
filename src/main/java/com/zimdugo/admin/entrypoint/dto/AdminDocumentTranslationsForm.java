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
public class AdminDocumentTranslationsForm {

    private List<TranslationForm> translations = new ArrayList<>();

    public List<AdminDocumentTranslationCommand> toCommands() {
        return translations == null ? List.of() : translations.stream()
            .map(TranslationForm::toCommand)
            .toList();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TranslationForm {

        private String language;
        private String title;
        private List<AdminDocumentTranslationForm.SectionForm> sections = new ArrayList<>();

        private AdminDocumentTranslationCommand toCommand() {
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
}
