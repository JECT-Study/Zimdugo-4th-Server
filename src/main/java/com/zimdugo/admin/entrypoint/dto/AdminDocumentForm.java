package com.zimdugo.admin.entrypoint.dto;

import com.zimdugo.admin.application.dto.AdminDocumentCommand;
import com.zimdugo.admin.application.dto.AdminDocumentFormResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class AdminDocumentForm {

    @NotBlank(message = "대표 제목은 필수 입력 항목입니다.")
    private String title;

    @NotBlank(message = "문서 타입은 필수 항목입니다.")
    private String type;

    private String typeDescription;

    @Size(max = 500)
    private String imageUrl;

    @Size(max = 10, message = "공지 이미지는 최대 10장까지 등록할 수 있습니다.")
    private List<String> imageOrder = new ArrayList<>();

    @Size(max = 10, message = "공지 이미지는 최대 10장까지 등록할 수 있습니다.")
    private List<MultipartFile> imageFiles = new ArrayList<>();

    private List<ImageForm> images = new ArrayList<>();

    private List<SectionForm> sections = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SectionForm {
        private String subtitle;

        private String content;
    }

    public record ImageForm(Long id, String imageUrl) {
    }

    public static AdminDocumentForm fromResult(AdminDocumentFormResult result) {
        AdminDocumentForm form = new AdminDocumentForm();
        form.setTitle(result.getTitle());
        form.setType(result.getType());
        form.setTypeDescription(result.getTypeDescription());
        form.setImageUrl(result.getImageUrl());
        form.setImages(result.getImages().stream()
            .map(image -> new ImageForm(image.id(), image.imageUrl()))
            .toList());

        List<SectionForm> sectionForms = new ArrayList<>();
        for (AdminDocumentFormResult.SectionResult section : result.getSections()) {
            SectionForm sectionForm = new SectionForm();
            sectionForm.setSubtitle(section.subtitle());
            sectionForm.setContent(section.content());
            sectionForms.add(sectionForm);
        }
        form.setSections(sectionForms);
        return form;
    }

    public AdminDocumentCommand toCommand() {
        return new AdminDocumentCommand(
            title,
            type,
            imageUrl,
            sections == null ? List.of() : sections.stream()
                .map(section -> new AdminDocumentCommand.SectionCommand(section.getSubtitle(), section.getContent()))
                .toList()
        );
    }
}
