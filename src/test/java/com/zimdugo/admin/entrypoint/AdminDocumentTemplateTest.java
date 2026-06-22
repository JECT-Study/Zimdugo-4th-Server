package com.zimdugo.admin.entrypoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminDocumentTemplateTest {

    @Test
    void noticeImagesStayLocalUntilMultipartFormSubmission() throws IOException {
        String template = Files.readString(
            Path.of("src/main/resources/templates/admin/form.html"),
            StandardCharsets.ISO_8859_1
        );

        assertThat(template)
            .contains("enctype=\"multipart/form-data\"")
            .contains("name=\"imageFiles\" multiple")
            .contains("id=\"uploadGallery\"")
            .contains("data-max-files=\"10\"")
            .contains("draggable=\"true\"")
            .contains("new DataTransfer()")
            .contains(".upload-item img")
            .contains("height: auto")
            .contains("data-max-pixels")
            .contains("image.naturalWidth * image.naturalHeight")
            .contains("top: 8px")
            .contains("right: 8px")
            .doesNotContain("top: -8px")
            .doesNotContain("right: -8px")
            .doesNotContain("aspect-ratio: 1")
            .doesNotContain("object-fit: cover")
            .doesNotContain("data-required-width")
            .doesNotContain("가로 1080px")
            .doesNotContain("/admin/api/uploads")
            .doesNotContain("XMLHttpRequest");
    }

    @Test
    void noticeImageHelpOnlyExplainsThumbnailReordering() throws IOException {
        String template = Files.readString(
            Path.of("src/main/resources/templates/admin/form.html"),
            StandardCharsets.UTF_8
        );

        assertThat(template)
            .contains("썸네일을 드래그해 순서를 바꿀 수 있습니다.")
            .doesNotContain("이미지는 선택 즉시 검사되며, 공지를 저장할 때만 S3에 업로드됩니다.");
    }

    @Test
    void noticeDetailKeepsOriginalImageRatio() throws IOException {
        String template = Files.readString(
            Path.of("src/main/resources/templates/admin/detail.html"),
            StandardCharsets.UTF_8
        );

        assertThat(template)
            .contains(".detail-images img")
            .contains("height: auto")
            .doesNotContain("aspect-ratio: 1")
            .doesNotContain("object-fit: cover");
    }
}
