package com.zimdugo.admin.application;

import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentRepository;
import com.zimdugo.admin.domain.AdminDocumentSection;
import com.zimdugo.admin.domain.DocumentType;
import com.zimdugo.admin.ui.dto.AdminDocumentForm;
import com.zimdugo.admin.ui.dto.AdminDocumentTranslationRequest;
import com.zimdugo.admin.ui.dto.AdminDocumentTranslationsResponse;
import com.zimdugo.admin.ui.dto.ClientDocumentResponse;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
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
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ADMIN_DOCUMENT_NOT_FOUND)
            );
    }

    @Test
    @DisplayName("공지사항(NOTICE) 문서는 개수에 상관없이 각각 개별적으로 활성화 상태를 토글할 수 있다")
    void toggleActiveForNotice() {
        // given
        AdminDocument doc1 = AdminDocument.builder()
            .title("공지 1")
            .type(DocumentType.NOTICE)
            .active(false)
            .build();
        addAllTranslations(doc1);
        adminDocumentRepository.save(doc1);

        AdminDocument doc2 = AdminDocument.builder()
            .title("공지 2")
            .type(DocumentType.NOTICE)
            .active(false)
            .build();
        addAllTranslations(doc2);
        adminDocumentRepository.save(doc2);

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
        AdminDocument terms1 = AdminDocument.builder()
            .title("약관 버전 1")
            .type(DocumentType.TERMS)
            .active(true)
            .build();
        addAllTranslations(terms1);
        adminDocumentRepository.save(terms1);

        AdminDocument terms2 = AdminDocument.builder()
            .title("약관 버전 2")
            .type(DocumentType.TERMS)
            .active(false)
            .build();
        addAllTranslations(terms2);
        adminDocumentRepository.save(terms2);

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
        addAllTranslations(doc);
        
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
        assertThat(doc.isActive()).isFalse();
    }

    @Test
    @DisplayName("문서 목록의 정렬 순서를 재배치하고 조회 시 listOrder ASC 및 createdAt DESC 순서로 정렬되어 반환된다")
    void reorderDocumentsAndSort() {
        // given
        AdminDocument doc1 = adminDocumentRepository.save(AdminDocument.builder()
            .title("문서 1")
            .type(DocumentType.NOTICE)
            .active(true)
            .build());
        AdminDocument doc2 = adminDocumentRepository.save(AdminDocument.builder()
            .title("문서 2")
            .type(DocumentType.NOTICE)
            .active(true)
            .build());
        AdminDocument doc3 = adminDocumentRepository.save(AdminDocument.builder()
            .title("문서 3")
            .type(DocumentType.NOTICE)
            .active(true)
            .build());

        // 기본 상태에서는 listOrder가 모두 0이므로 최신 생성된 순서로 나옵니다 (doc3 -> doc2 -> doc1)
        List<AdminDocument> initialList = adminDocumentService.getDocumentsByType(DocumentType.NOTICE);
        assertThat(initialList.get(0).getTitle()).isEqualTo("문서 3");
        assertThat(initialList.get(1).getTitle()).isEqualTo("문서 2");
        assertThat(initialList.get(2).getTitle()).isEqualTo("문서 1");

        // when: 순서를 doc1 -> doc3 -> doc2 순서대로 지정하여 재배치
        adminDocumentService.reorderDocuments(List.of(doc1.getId(), doc3.getId(), doc2.getId()));

        // then: 조회 결과가 재배치한 listOrder 오름차순 기준으로 정렬되어 반환된다 (doc1 -> doc3 -> doc2)
        List<AdminDocument> sortedList = adminDocumentService.getDocumentsByType(DocumentType.NOTICE);
        assertThat(sortedList.get(0).getTitle()).isEqualTo("문서 1");
        assertThat(sortedList.get(0).getListOrder()).isEqualTo(0);
        assertThat(sortedList.get(1).getTitle()).isEqualTo("문서 3");
        assertThat(sortedList.get(1).getListOrder()).isEqualTo(1);
        assertThat(sortedList.get(2).getTitle()).isEqualTo("문서 2");
        assertThat(sortedList.get(2).getListOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("공지사항이 아닌 다른 타입의 문서를 재배치하려고 하면 요청 오류가 발생한다")
    void reorderNonNoticeDocumentsThrowsException() {
        // given
        AdminDocument terms = adminDocumentRepository.save(AdminDocument.builder()
            .title("이용약관")
            .type(DocumentType.TERMS)
            .active(true)
            .build());

        // when & then
        assertThatThrownBy(() -> adminDocumentService.reorderDocuments(List.of(terms.getId())))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_ADMIN_DOCUMENT_ORDER)
            );
    }

    @Test
    @DisplayName("요청 언어의 번역만 반환한다")
    void localizesDocumentWithRequestedLanguageOnly() {
        AdminDocumentSection section1 = section("원문 소제목 1", "원문 내용 1", 0);
        AdminDocumentSection section2 = section("원문 소제목 2", "원문 내용 2", 1);
        AdminDocument document = adminDocumentRepository.save(AdminDocument.builder()
            .title("원문 제목")
            .type(DocumentType.NOTICE)
            .active(true)
            .sections(List.of(section1, section2))
            .build());

        document.upsertTranslation("ko", "한국어 제목");
        section1.upsertTranslation("ko", "한국어 소제목 1", "한국어 내용 1");
        section2.upsertTranslation("ko", "한국어 소제목 2", "한국어 내용 2");

        ClientDocumentResponse response = adminDocumentService
            .getLocalizedActiveDocumentsByType(DocumentType.NOTICE, SupportedLanguage.KOREAN)
            .getFirst();

        assertThat(response.getTitle()).isEqualTo("한국어 제목");
        assertThat(response.getSections().get(0).getContent()).isEqualTo("한국어 내용 1");
        assertThat(response.getSections().get(1).getContent()).isEqualTo("한국어 내용 2");
    }

    @Test
    @DisplayName("요청 언어 번역이 누락되면 다른 언어로 대체하지 않는다")
    void rejectsMissingRequestedLanguageTranslation() {
        AdminDocumentSection section = section("원문 소제목", "원문 내용", 0);
        AdminDocument document = adminDocumentRepository.save(AdminDocument.builder()
            .title("원문 제목")
            .type(DocumentType.NOTICE)
            .active(true)
            .sections(List.of(section))
            .build());
        document.upsertTranslation("en", "English title");
        section.upsertTranslation("en", "English subtitle", "English content");

        assertThatThrownBy(() -> adminDocumentService
            .getLocalizedActiveDocumentsByType(DocumentType.NOTICE, SupportedLanguage.JAPANESE))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.I18N_TRANSLATION_MISSING)
            );
    }

    @Test
    @DisplayName("관리자 번역 PUT은 언어 태그를 정규화하고 누락된 섹션 번역을 제거한다")
    void putTranslationReplacesLanguageTranslation() {
        AdminDocumentSection section1 = section("원문 소제목 1", "원문 내용 1", 0);
        AdminDocumentSection section2 = section("원문 소제목 2", "원문 내용 2", 1);
        AdminDocument document = adminDocumentRepository.saveAndFlush(AdminDocument.builder()
            .title("원문 제목")
            .type(DocumentType.NOTICE)
            .sections(List.of(section1, section2))
            .build());

        adminDocumentService.putTranslation(document.getId(), translationRequest(
            "en",
            "US title",
            sectionTranslation(section1.getId(), "US subtitle 1", "US content 1"),
            sectionTranslation(section2.getId(), "US subtitle 2", "US content 2")
        ));

        AdminDocumentTranslationsResponse response = adminDocumentService.putTranslation(
            document.getId(),
            translationRequest(
                "en",
                "Updated US title",
                sectionTranslation(section1.getId(), "Updated subtitle 1", "Updated content 1")
            )
        );

        assertThat(response.getTranslations()).singleElement().satisfies(translation -> {
            assertThat(translation.getLanguage()).isEqualTo("en");
            assertThat(translation.getTitle()).isEqualTo("Updated US title");
            assertThat(translation.getSections()).singleElement().satisfies(section -> {
                assertThat(section.getSectionId()).isEqualTo(section1.getId());
                assertThat(section.getContent()).isEqualTo("Updated content 1");
            });
        });
        assertThat(section2.getTranslations()).isEmpty();
    }

    @Test
    @DisplayName("다른 문서의 섹션 ID로 번역을 저장할 수 없다")
    void putTranslationRejectsForeignSection() {
        AdminDocument document = adminDocumentRepository.saveAndFlush(AdminDocument.builder()
            .title("문서")
            .type(DocumentType.NOTICE)
            .sections(List.of(section("소제목", "내용", 0)))
            .build());
        AdminDocument otherDocument = adminDocumentRepository.saveAndFlush(AdminDocument.builder()
            .title("다른 문서")
            .type(DocumentType.NOTICE)
            .sections(List.of(section("다른 소제목", "다른 내용", 0)))
            .build());
        Long foreignSectionId = otherDocument.getSections().getFirst().getId();

        assertThatThrownBy(() -> adminDocumentService.putTranslation(
            document.getId(),
            translationRequest("en", "Title", sectionTranslation(foreignSectionId, "Subtitle", "Content"))
        ))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_ADMIN_DOCUMENT_TRANSLATION)
            );
    }

    private AdminDocumentSection section(String subtitle, String content, int listOrder) {
        return AdminDocumentSection.builder()
            .subtitle(subtitle)
            .content(content)
            .listOrder(listOrder)
            .build();
    }

    private AdminDocumentTranslationRequest translationRequest(
        String language,
        String title,
        AdminDocumentTranslationRequest.SectionTranslationRequest... sections
    ) {
        AdminDocumentTranslationRequest request = new AdminDocumentTranslationRequest();
        request.setLanguage(language);
        request.setTitle(title);
        request.setSections(List.of(sections));
        return request;
    }

    private AdminDocumentTranslationRequest.SectionTranslationRequest sectionTranslation(
        Long sectionId,
        String subtitle,
        String content
    ) {
        AdminDocumentTranslationRequest.SectionTranslationRequest request =
            new AdminDocumentTranslationRequest.SectionTranslationRequest();
        request.setSectionId(sectionId);
        request.setSubtitle(subtitle);
        request.setContent(content);
        return request;
    }

    @Test
    @DisplayName("지원 언어 번역이 하나라도 완료되지 않은 문서는 활성화할 수 없다")
    void toggleActiveWithoutRequiredTranslationsThrowsException() {
        // given
        AdminDocument doc = adminDocumentRepository.save(AdminDocument.builder()
            .title("번역 없는 문서")
            .type(DocumentType.NOTICE)
            .active(false)
            .build());

        // when & then
        assertThatThrownBy(() -> adminDocumentService.toggleActive(doc.getId()))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CANNOT_ACTIVATE_WITHOUT_REQUIRED_TRANSLATIONS)
            );
    }

    private void addAllTranslations(AdminDocument document) {
        for (SupportedLanguage language : SupportedLanguage.all()) {
            document.upsertTranslation(language.languageTag(), language.languageTag() + " title");
            for (AdminDocumentSection section : document.getSections()) {
                section.upsertTranslation(
                    language.languageTag(),
                    language.languageTag() + " subtitle",
                    language.languageTag() + " content"
                );
            }
        }
    }
}
