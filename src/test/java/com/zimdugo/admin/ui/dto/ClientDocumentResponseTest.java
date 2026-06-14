package com.zimdugo.admin.ui.dto;

import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentSection;
import com.zimdugo.admin.domain.DocumentType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientDocumentResponseTest {

    @Test
    @DisplayName("AdminDocument 엔티티를 전달하면 ClientDocumentResponse DTO로 필드 값들이 올바르게 조립 및 매핑된다")
    void assembleClientDocumentResponseFromEntity() {
        // given
        AdminDocumentSection section1 = AdminDocumentSection.builder()
            .subtitle("소제목 1")
            .content("내용 1")
            .listOrder(0)
            .build();
        AdminDocumentSection section2 = AdminDocumentSection.builder()
            .subtitle("소제목 2")
            .content("내용 2")
            .listOrder(1)
            .build();

        AdminDocument document = AdminDocument.builder()
            .title("테스트 문서 제목")
            .type(DocumentType.NOTICE)
            .active(true)
            .sections(List.of(section1, section2))
            .build();

        // when
        ClientDocumentResponse response = new ClientDocumentResponse(document);

        // then
        assertThat(response.getId()).isNull();
        assertThat(response.getTitle()).isEqualTo("테스트 문서 제목");
        assertThat(response.getType()).isEqualTo(DocumentType.NOTICE);
        assertThat(response.getSections()).hasSize(2);

        ClientDocumentResponse.SectionResponse secResp1 = response.getSections().get(0);
        assertThat(secResp1.getSubtitle()).isEqualTo("소제목 1");
        assertThat(secResp1.getContent()).isEqualTo("내용 1");

        ClientDocumentResponse.SectionResponse secResp2 = response.getSections().get(1);
        assertThat(secResp2.getSubtitle()).isEqualTo("소제목 2");
        assertThat(secResp2.getContent()).isEqualTo("내용 2");
    }
}
