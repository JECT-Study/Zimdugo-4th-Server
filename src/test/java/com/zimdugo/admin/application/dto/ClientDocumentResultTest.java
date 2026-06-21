package com.zimdugo.admin.application.dto;

import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentSection;
import com.zimdugo.admin.domain.DocumentType;
import com.zimdugo.common.i18n.SupportedLanguage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientDocumentResultTest {

    @Test
    @DisplayName("AdminDocument 엔티티를 전달하면 ClientDocumentResult로 필드 값들이 올바르게 조립 및 매핑된다")
    void assembleClientDocumentResultFromEntity() {
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
        document.updateImageUrl("https://cdn.example.com/admin/notice-images/notice.jpg");
        document.replaceImages(List.of(
            "https://cdn.example.com/admin/notice-images/notice.jpg",
            "https://cdn.example.com/admin/notice-images/detail.jpg"
        ));
        document.upsertTranslation("ko", "테스트 문서 제목");
        section1.upsertTranslation("ko", "소제목 1", "내용 1");
        section2.upsertTranslation("ko", "소제목 2", "내용 2");

        // when
        ClientDocumentResult response = ClientDocumentResult.from(document, SupportedLanguage.KOREAN);

        // then
        assertThat(response.getId()).isNull();
        assertThat(response.getTitle()).isEqualTo("테스트 문서 제목");
        assertThat(response.getType()).isEqualTo(DocumentType.NOTICE.name());
        assertThat(response.getImageUrl()).isEqualTo("https://cdn.example.com/admin/notice-images/notice.jpg");
        assertThat(response.getImageUrls()).containsExactly(
            "https://cdn.example.com/admin/notice-images/notice.jpg",
            "https://cdn.example.com/admin/notice-images/detail.jpg"
        );
        assertThat(response.getSections()).hasSize(2);

        ClientDocumentResult.SectionResult secResp1 = response.getSections().get(0);
        assertThat(secResp1.getSubtitle()).isEqualTo("소제목 1");
        assertThat(secResp1.getContent()).isEqualTo("내용 1");

        ClientDocumentResult.SectionResult secResp2 = response.getSections().get(1);
        assertThat(secResp2.getSubtitle()).isEqualTo("소제목 2");
        assertThat(secResp2.getContent()).isEqualTo("내용 2");
    }
}
