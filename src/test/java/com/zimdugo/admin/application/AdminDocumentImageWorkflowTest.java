package com.zimdugo.admin.application;

import com.zimdugo.admin.application.dto.AdminDocumentCommand;
import com.zimdugo.admin.application.dto.AdminDocumentDetailResult;
import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.DocumentType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminDocumentImageWorkflowTest {

    @Mock
    private AdminDocumentService documentService;

    @Mock
    private AdminNoticeImageStorage imageStorage;

    @Mock
    private AdminDocumentDetailResult result;

    @InjectMocks
    private AdminDocumentImageWorkflow workflow;

    @Test
    void createUploadsOnSaveAndKeepsRequestedOrder() {
        MockMultipartFile first = file("first.png");
        MockMultipartFile second = file("second.png");
        given(imageStorage.uploadAll(List.of(first, second))).willReturn(List.of("url-1", "url-2"));
        AdminDocumentCommand command = command();
        given(documentService.createDocumentResult(command.withImageUrls(List.of("url-2", "url-1"))))
            .willReturn(result);

        workflow.create(command, List.of(first, second), List.of("new:1", "new:0"));

        verify(documentService).createDocumentResult(command.withImageUrls(List.of("url-2", "url-1")));
    }

    @Test
    void createDeletesUploadedImagesWhenDatabaseSaveFails() {
        MockMultipartFile file = file("notice.png");
        AdminDocumentCommand command = command();
        given(imageStorage.uploadAll(List.of(file))).willReturn(List.of("uploaded-url"));
        given(documentService.createDocumentResult(command.withImageUrls(List.of("uploaded-url"))))
            .willThrow(new IllegalStateException("database failure"));

        assertThatThrownBy(() -> workflow.create(command, List.of(file), List.of("new:0")))
            .isInstanceOf(IllegalStateException.class);

        verify(imageStorage).deleteAll(List.of("uploaded-url"));
    }

    @Test
    void updateDeletesRemovedExistingImageAfterSave() {
        AdminDocument document = AdminDocument.builder()
            .type(DocumentType.NOTICE)
            .title("공지")
            .build();
        document.replaceImages(List.of("old-1", "old-2"));
        given(documentService.getDocumentImageUrls(1L)).willReturn(document.getImageUrls());
        given(imageStorage.uploadAll(List.of())).willReturn(List.of());
        AdminDocumentCommand command = command();
        given(documentService.updateDocumentResult(1L, command.withImageUrls(List.of("old-2"))))
            .willReturn(result);

        workflow.update(1L, command, List.of(), List.of("existing:1"));

        verify(imageStorage).deleteAll(List.of("old-1"));
    }

    private AdminDocumentCommand command() {
        return new AdminDocumentCommand("공지", "NOTICE", null, List.of(), List.of());
    }

    private MockMultipartFile file(String fileName) {
        return new MockMultipartFile("imageFiles", fileName, "image/png", new byte[]{1});
    }
}
