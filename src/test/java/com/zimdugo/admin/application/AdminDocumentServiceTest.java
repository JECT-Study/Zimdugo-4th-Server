package com.zimdugo.admin.application;

import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentRepository;
import com.zimdugo.admin.domain.DocumentType;
import com.zimdugo.admin.ui.dto.AdminDocumentForm;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(AdminDocumentService.class)
@Transactional
class AdminDocumentServiceTest {

    @Autowired
    private AdminDocumentService adminDocumentService;

    @Autowired
    private AdminDocumentRepository adminDocumentRepository;

    @Test
    @DisplayName("새로운 문서를 저장할 때 1:N 관계의 덩어리(Section)들도 함께 저장된다")
    void createDocumentWithSections() {
        // given
        AdminDocumentForm form = new AdminDocumentForm();
        form.setTitle("테스트 공지사항");
        form.setType(DocumentType.NOTICE);

        AdminDocumentForm.SectionForm sec1 = new AdminDocumentForm.SectionForm();
        sec1.setSubtitle("소제목 1");
        sec1.setContent("내용 1");

        AdminDocumentForm.SectionForm sec2 = new AdminDocumentForm.SectionForm();
        sec2.setSubtitle("소제목 2");
        sec2.setContent("내용 2");

        form.setSections(List.of(sec1, sec2));

        // when
        AdminDocument saved = adminDocumentService.createDocument(form);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("테스트 공지사항");
        assertThat(saved.getType()).isEqualTo(DocumentType.NOTICE);
        assertThat(saved.getSections()).hasSize(2);
        assertThat(saved.getSections().get(0).getSubtitle()).isEqualTo("소제목 1");
        assertThat(saved.getSections().get(0).getListOrder()).isEqualTo(0);
        assertThat(saved.getSections().get(1).getSubtitle()).isEqualTo("소제목 2");
        assertThat(saved.getSections().get(1).getListOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("문서를 수정할 때 기존 덩어리들이 지워지고 새로운 덩어리로 갱신된다")
    void updateDocumentSections() {
        // given
        AdminDocumentForm createForm = new AdminDocumentForm();
        createForm.setTitle("최초 공지");
        createForm.setType(DocumentType.NOTICE);

        AdminDocumentForm.SectionForm sec1 = new AdminDocumentForm.SectionForm();
        sec1.setSubtitle("구 소제목");
        sec1.setContent("구 내용");
        createForm.setSections(List.of(sec1));

        AdminDocument saved = adminDocumentService.createDocument(createForm);
        Long docId = saved.getId();

        // 영속성 컨텍스트 초기화를 위해 레포지토리를 직접 다루거나 서비스를 호출하여 다시 로드
        AdminDocument documentToUpdate = adminDocumentService.getById(docId);
        assertThat(documentToUpdate.getSections()).hasSize(1);

        // when (수정 DTO 빌드)
        AdminDocumentForm updateForm = new AdminDocumentForm();
        updateForm.setTitle("수정된 공지");
        updateForm.setType(DocumentType.NOTICE);

        AdminDocumentForm.SectionForm newSec1 = new AdminDocumentForm.SectionForm();
        newSec1.setSubtitle("새 소제목 1");
        newSec1.setContent("새 내용 1");

        AdminDocumentForm.SectionForm newSec2 = new AdminDocumentForm.SectionForm();
        newSec2.setSubtitle("새 소제목 2");
        newSec2.setContent("새 내용 2");

        updateForm.setSections(List.of(newSec1, newSec2));

        AdminDocument updated = adminDocumentService.updateDocument(docId, updateForm);

        // then
        assertThat(updated.getTitle()).isEqualTo("수정된 공지");
        assertThat(updated.getSections()).hasSize(2);
        assertThat(updated.getSections().get(0).getSubtitle()).isEqualTo("새 소제목 1");
        assertThat(updated.getSections().get(1).getSubtitle()).isEqualTo("새 소제목 2");
    }

    @Test
    @DisplayName("문서를 삭제하면 1:N 관계의 덩어리들도 DB에서 함께 자동 삭제된다")
    void deleteDocumentAndOrphanRemoval() {
        // given
        AdminDocumentForm form = new AdminDocumentForm();
        form.setTitle("삭제할 문서");
        form.setType(DocumentType.PRIVACY);

        AdminDocumentForm.SectionForm sec1 = new AdminDocumentForm.SectionForm();
        sec1.setSubtitle("약관 내용");
        sec1.setContent("중요 개인정보 처리 방침 본문");
        form.setSections(List.of(sec1));

        AdminDocument saved = adminDocumentService.createDocument(form);
        Long docId = saved.getId();

        // when
        adminDocumentService.deleteDocument(docId);

        // then
        assertThatThrownBy(() -> adminDocumentService.getById(docId))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("공지사항(NOTICE) 문서는 개수에 상관없이 각각 개별적으로 활성화 상태를 토글할 수 있다")
    void toggleActiveForNotice() {
        // given
        AdminDocument doc1 = adminDocumentRepository.save(AdminDocument.builder()
            .title("공지 1")
            .type(DocumentType.NOTICE)
            .active(false)
            .build());
        AdminDocument doc2 = adminDocumentRepository.save(AdminDocument.builder()
            .title("공지 2")
            .type(DocumentType.NOTICE)
            .active(false)
            .build());

        // when & then: 1번 공지 활성화
        adminDocumentService.toggleActive(doc1.getId());
        assertThat(doc1.isActive()).isTrue();
        assertThat(doc2.isActive()).isFalse();

        // when & then: 2번 공지 활성화
        adminDocumentService.toggleActive(doc2.getId());
        assertThat(doc1.isActive()).isTrue();
        assertThat(doc2.isActive()).isTrue();

        // when & then: 1번 공지 비활성화
        adminDocumentService.toggleActive(doc1.getId());
        assertThat(doc1.isActive()).isFalse();
        assertThat(doc2.isActive()).isTrue();
    }

    @Test
    @DisplayName("이용 약관(TERMS) 문서를 활성화하면 기존에 활성화되어 있던 다른 약관 문서는 자동으로 비활성화된다")
    void toggleActiveForTermsExclusivity() {
        // given
        AdminDocument terms1 = adminDocumentRepository.save(AdminDocument.builder()
            .title("약관 버전 1")
            .type(DocumentType.TERMS)
            .active(true)
            .build());
        AdminDocument terms2 = adminDocumentRepository.save(AdminDocument.builder()
            .title("약관 버전 2")
            .type(DocumentType.TERMS)
            .active(false)
            .build());

        assertThat(terms1.isActive()).isTrue();
        assertThat(terms2.isActive()).isFalse();

        // when: 약관 버전 2 활성화
        adminDocumentService.toggleActive(terms2.getId());

        // then: 약관 버전 2가 활성화되고, 기존 활성화 상태였던 약관 버전 1은 비활성화된다
        assertThat(terms1.isActive()).isFalse();
        assertThat(terms2.isActive()).isTrue();
    }

    @Test
    @DisplayName("토글로 적용 여부 상태를 바꿀 때 appliedAt이 갱신되지만 updatedAt은 변경되지 않고, 본문 수정 시에만 updatedAt이 갱신된다")
    void appliedAtAndUpdatedAtSegregation() throws InterruptedException {
        // given
        AdminDocumentForm form = new AdminDocumentForm();
        form.setTitle("최초 생성");
        form.setType(DocumentType.NOTICE);
        AdminDocument doc = adminDocumentService.createDocument(form);
        
        java.time.LocalDateTime initialUpdatedAt = doc.getUpdatedAt();
        assertThat(doc.getAppliedAt()).isNull();

        Thread.sleep(10);

        // when: 적용 여부 활성화 토글 실행
        adminDocumentService.toggleActive(doc.getId());

        // then: active와 appliedAt은 갱신되지만, updatedAt은 초기와 동일해야 함
        assertThat(doc.isActive()).isTrue();
        assertThat(doc.getAppliedAt()).isNotNull();
        assertThat(doc.getUpdatedAt()).isEqualTo(initialUpdatedAt);

        Thread.sleep(10);

        // when: 본문 내용 수정 실행
        AdminDocumentForm updateForm = new AdminDocumentForm();
        updateForm.setTitle("수정된 제목");
        updateForm.setType(DocumentType.NOTICE);
        adminDocumentService.updateDocument(doc.getId(), updateForm);

        // then: updatedAt이 새롭게 갱신되어 초기 수정 시점보다 미래여야 함
        assertThat(doc.getUpdatedAt()).isAfter(initialUpdatedAt);
    }
}
