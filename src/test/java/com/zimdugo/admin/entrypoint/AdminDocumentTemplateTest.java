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
            .doesNotContain("/admin/api/uploads")
            .doesNotContain("XMLHttpRequest");
    }
}
