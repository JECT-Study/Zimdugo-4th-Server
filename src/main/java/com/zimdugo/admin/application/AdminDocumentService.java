package com.zimdugo.admin.application;

import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentRepository;
import com.zimdugo.admin.domain.AdminDocumentSection;
import com.zimdugo.admin.domain.DocumentLanguage;
import com.zimdugo.admin.domain.DocumentType;
import com.zimdugo.admin.application.dto.AdminDocumentCommand;
import com.zimdugo.admin.application.dto.AdminDocumentDetailResult;
import com.zimdugo.admin.application.dto.AdminDocumentFormResult;
import com.zimdugo.admin.application.dto.AdminDocumentSummaryResult;
import com.zimdugo.admin.application.dto.AdminDocumentTranslationCommand;
import com.zimdugo.admin.application.dto.AdminDocumentTranslationsResult;
import com.zimdugo.admin.application.dto.AdminDocumentTypeResult;
import com.zimdugo.admin.application.dto.ClientDocumentResult;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDocumentService {

    private final AdminDocumentRepository adminDocumentRepository;
    private final AdminNoticeImageValidator adminNoticeImageValidator;

    public AdminDocumentTypeResult getDocumentType(String type) {
        return AdminDocumentTypeResult.from(toDocumentType(type));
    }

    public List<AdminDocumentSummaryResult> getDocumentSummaries(String type) {
        return getDocumentsByType(toDocumentType(type)).stream()
            .map(AdminDocumentSummaryResult::from)
            .toList();
    }

    public List<AdminDocument> getDocumentsByType(DocumentType type) {
        return adminDocumentRepository.findByType(type);
    }

    public List<AdminDocument> getActiveDocumentsByType(DocumentType type) {
        return adminDocumentRepository.findByTypeAndActive(type, true);
    }

    public List<ClientDocumentResult> getLocalizedActiveDocumentsByType(
        String type,
        SupportedLanguage requestedLanguage
    ) {
        List<AdminDocument> documents = adminDocumentRepository.findByTypeAndActive(toDocumentType(type), true);
        boolean hasMissingTranslation = documents.stream()
            .anyMatch(document -> !document.hasCompleteTranslation(requestedLanguage.languageTag()));
        if (hasMissingTranslation) {
            throw new BusinessException(ErrorCode.I18N_TRANSLATION_MISSING);
        }
        return documents.stream()
            .map(document -> ClientDocumentResult.from(document, requestedLanguage))
            .toList();
    }

    public AdminDocument getById(Long id) {
        return adminDocumentRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_DOCUMENT_NOT_FOUND));
    }

    public AdminDocumentDetailResult getDocumentDetail(Long id) {
        return AdminDocumentDetailResult.from(getById(id));
    }

    public AdminDocumentFormResult getNewDocumentForm(String type) {
        return AdminDocumentFormResult.newDocument(AdminDocumentTypeResult.from(toDocumentType(type)));
    }

    public AdminDocumentFormResult getDocumentForm(Long id) {
        return AdminDocumentFormResult.from(getDocumentDetail(id));
    }

    public AdminDocumentTranslationsResult getTranslations(Long id) {
        return AdminDocumentTranslationsResult.from(getById(id));
    }

    @Transactional
    public AdminDocument createDocument(AdminDocumentCommand command) {
        adminNoticeImageValidator.validate(command.imageUrl());
        AdminDocument document = toEntity(command);
        return adminDocumentRepository.save(document);
    }

    @Transactional
    public AdminDocumentDetailResult createDocumentResult(AdminDocumentCommand command) {
        return AdminDocumentDetailResult.from(createDocument(command));
    }

    @Transactional
    public AdminDocument updateDocument(Long id, AdminDocumentCommand command) {
        AdminDocument document = getById(id);
        
        List<AdminDocumentSection> newSections = new ArrayList<>();
        if (command.sections() != null) {
            for (int i = 0; i < command.sections().size(); i++) {
                AdminDocumentCommand.SectionCommand sectionCommand = command.sections().get(i);
                if (sectionCommand.content() != null && !sectionCommand.content().isBlank()) {
                    newSections.add(AdminDocumentSection.builder()
                        .subtitle(sectionCommand.subtitle())
                        .content(sectionCommand.content())
                        .listOrder(i)
                        .build());
                }
            }
        }
        
        adminNoticeImageValidator.validate(command.imageUrl());
        document.update(command.title(), command.imageUrl(), newSections);
        if (document.isActive()) {
            document.deactivate();
        }
        return document;
    }

    @Transactional
    public AdminDocumentDetailResult updateDocumentResult(Long id, AdminDocumentCommand command) {
        return AdminDocumentDetailResult.from(updateDocument(id, command));
    }

    @Transactional
    public AdminDocumentTranslationsResult putTranslation(Long id, AdminDocumentTranslationCommand command) {
        AdminDocument document = getById(id);
        String language = DocumentLanguage.normalize(command.language());
        document.upsertTranslation(language, command.title());

        Map<Long, AdminDocumentSection> sectionsById = new HashMap<>();
        for (AdminDocumentSection section : document.getSections()) {
            sectionsById.put(section.getId(), section);
        }

        Set<Long> translatedSectionIds = new HashSet<>();
        List<AdminDocumentTranslationCommand.SectionTranslationCommand> sectionRequests = command.sectionsOrEmpty();
        for (AdminDocumentTranslationCommand.SectionTranslationCommand sectionRequest : sectionRequests) {
            AdminDocumentSection section = sectionsById.get(sectionRequest.sectionId());
            if (section == null) {
                throw new BusinessException(ErrorCode.INVALID_ADMIN_DOCUMENT_TRANSLATION);
            }
            if (!translatedSectionIds.add(sectionRequest.sectionId())) {
                throw new BusinessException(ErrorCode.INVALID_ADMIN_DOCUMENT_TRANSLATION);
            }
            section.upsertTranslation(language, sectionRequest.subtitle(), sectionRequest.content());
        }

        document.getSections().stream()
            .filter(section -> !translatedSectionIds.contains(section.getId()))
            .forEach(section -> section.removeTranslation(language));
        if (document.isActive() && !document.hasAllRequiredTranslations()) {
            throw new BusinessException(ErrorCode.CANNOT_ACTIVATE_WITHOUT_REQUIRED_TRANSLATIONS);
        }
        return AdminDocumentTranslationsResult.from(document);
    }

    @Transactional
    public void deleteDocument(Long id) {
        AdminDocument document = getById(id);
        adminDocumentRepository.delete(document);
    }

    @Transactional
    public void toggleActive(Long id) {
        AdminDocument document = getById(id);
        boolean nextActiveState = !document.isActive();
        
        if (nextActiveState) {
            if (!document.hasAllRequiredTranslations()) {
                throw new BusinessException(
                    ErrorCode.CANNOT_ACTIVATE_WITHOUT_REQUIRED_TRANSLATIONS,
                    missingTranslationMessage(document)
                );
            }
            if (document.getType() == DocumentType.TERMS || document.getType() == DocumentType.PRIVACY) {
                List<AdminDocument> activeDocs = adminDocumentRepository
                    .findByTypeAndActive(document.getType(), true);
                for (AdminDocument doc : activeDocs) {
                    doc.deactivate();
                }
            }
            document.activate();
        } else {
            document.deactivate();
        }
    }

    private String missingTranslationMessage(AdminDocument document) {
        List<String> missingLanguages = SupportedLanguage.all().stream()
            .filter(language -> !document.hasCompleteTranslation(language.languageTag()))
            .map(SupportedLanguage::languageTag)
            .toList();

        return "번역이 완료되지 않아 적용할 수 없습니다. 누락 언어: "
            + String.join(", ", missingLanguages)
            + ". 번역 확인 화면에서 제목과 모든 섹션 본문을 저장해 주세요.";
    }

    @Transactional
    public void reorderDocuments(List<Long> documentIds) {
        for (int i = 0; i < documentIds.size(); i++) {
            AdminDocument document = getById(documentIds.get(i));
            if (document.getType() != DocumentType.NOTICE) {
                throw new BusinessException(ErrorCode.INVALID_ADMIN_DOCUMENT_ORDER);
            }
            document.updateListOrder(i);
        }
    }

    private AdminDocument toEntity(AdminDocumentCommand command) {
        List<AdminDocumentSection> sectionEntities = new ArrayList<>();
        List<AdminDocumentCommand.SectionCommand> sections = command.sectionsOrEmpty();
        for (int i = 0; i < sections.size(); i++) {
            AdminDocumentCommand.SectionCommand section = sections.get(i);
            if (section.content() != null && !section.content().isBlank()) {
                sectionEntities.add(AdminDocumentSection.builder()
                    .subtitle(section.subtitle())
                    .content(section.content())
                    .listOrder(i)
                    .build());
            }
        }
        AdminDocument document = AdminDocument.builder()
            .title(command.title())
            .type(toDocumentType(command.type()))
            .sections(sectionEntities)
            .build();
        document.updateImageUrl(command.imageUrl());
        return document;
    }

    private DocumentType toDocumentType(String type) {
        try {
            return DocumentType.valueOf(type);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BusinessException(ErrorCode.INVALID_ADMIN_DOCUMENT_TYPE);
        }
    }
}
