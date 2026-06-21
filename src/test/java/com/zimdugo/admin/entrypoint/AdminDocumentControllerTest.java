package com.zimdugo.admin.entrypoint;

import com.zimdugo.admin.application.AdminDocumentImageWorkflow;
import com.zimdugo.admin.application.AdminDocumentService;
import com.zimdugo.admin.application.AdminNoticeImageProperties;
import com.zimdugo.admin.application.dto.AdminDocumentDetailResult;
import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.DocumentType;
import com.zimdugo.common.storage.S3StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminDocumentControllerTest {

    @Mock
    private AdminDocumentService documentService;

    @Mock
    private AdminDocumentImageWorkflow imageWorkflow;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminDocumentController controller = new AdminDocumentController(
            documentService,
            imageWorkflow,
            new AdminNoticeImageProperties(50_000_000),
            storageProperties()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createBindsOrderedMultipartNoticeImages() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
            "imageFiles",
            "notice.png",
            "image/png",
            new byte[]{1}
        );
        given(imageWorkflow.create(any(), any(), any())).willReturn(detail());

        mockMvc.perform(multipart("/admin/documents")
                .file(image)
                .param("title", "이미지 공지")
                .param("type", "NOTICE")
                .param("imageOrder", "new:0"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/documents?type=NOTICE"));

        verify(imageWorkflow).create(any(), any(), any());
    }

    private AdminDocumentDetailResult detail() {
        return AdminDocumentDetailResult.from(AdminDocument.builder()
            .type(DocumentType.NOTICE)
            .title("이미지 공지")
            .build());
    }

    private S3StorageProperties storageProperties() {
        return new S3StorageProperties(
            "ap-northeast-2",
            "bucket",
            "https://cdn.example.com",
            10,
            10 * 1024 * 1024
        );
    }
}
