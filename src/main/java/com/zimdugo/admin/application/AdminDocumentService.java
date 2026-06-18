package com.zimdugo.admin.application;

import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentRepository;
import com.zimdugo.admin.domain.AdminDocumentSection;
import com.zimdugo.admin.domain.DocumentLanguage;
import com.zimdugo.admin.domain.DocumentType;
import com.zimdugo.admin.ui.dto.AdminDocumentForm;
import com.zimdugo.admin.ui.dto.AdminDocumentTranslationRequest;
import com.zimdugo.admin.ui.dto.AdminDocumentTranslationsResponse;
import com.zimdugo.admin.ui.dto.ClientDocumentResponse;
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

    public List<AdminDocument> getDocumentsByType(DocumentType type) {
        return adminDocumentRepository.findByType(type);
    }

    public List<AdminDocument> getActiveDocumentsByType(DocumentType type) {
        return adminDocumentRepository.findByTypeAndActive(type, true);
    }

    public List<ClientDocumentResponse> getLocalizedActiveDocumentsByType(
        DocumentType type,
        SupportedLanguage requestedLanguage
    ) {
        List<AdminDocument> documents = adminDocumentRepository.findByTypeAndActive(type, true);
        boolean hasMissingTranslation = documents.stream()
            .anyMatch(document -> !document.hasCompleteTranslation(requestedLanguage.languageTag()));
        if (hasMissingTranslation) {
            throw new BusinessException(ErrorCode.I18N_TRANSLATION_MISSING);
        }
        return documents.stream()
            .map(document -> new ClientDocumentResponse(document, requestedLanguage))
            .toList();
    }

    public AdminDocument getById(Long id) {
        return adminDocumentRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_DOCUMENT_NOT_FOUND));
    }

    public AdminDocumentTranslationsResponse getTranslations(Long id) {
        return AdminDocumentTranslationsResponse.from(getById(id));
    }

    @Transactional
    public AdminDocument createDocument(AdminDocumentForm form) {
        adminNoticeImageValidator.validate(form.getImageUrl());
        AdminDocument document = form.toEntity();
        return adminDocumentRepository.save(document);
    }

    @Transactional
    public AdminDocument updateDocument(Long id, AdminDocumentForm form) {
        AdminDocument document = getById(id);
        
        List<AdminDocumentSection> newSections = new ArrayList<>();
        if (form.getSections() != null) {
            for (int i = 0; i < form.getSections().size(); i++) {
                AdminDocumentForm.SectionForm secForm = form.getSections().get(i);
                if (secForm.getContent() != null && !secForm.getContent().isBlank()) {
                    newSections.add(AdminDocumentSection.builder()
                        .subtitle(secForm.getSubtitle())
                        .content(secForm.getContent())
                        .listOrder(i)
                        .build());
                }
            }
        }
        
        adminNoticeImageValidator.validate(form.getImageUrl());
        document.update(form.getTitle(), form.getImageUrl(), newSections);
        if (document.isActive()) {
            document.deactivate();
        }
        return document;
    }

    @Transactional
    public AdminDocumentTranslationsResponse putTranslation(Long id, AdminDocumentTranslationRequest request) {
        AdminDocument document = getById(id);
        String language = DocumentLanguage.normalize(request.getLanguage());
        document.upsertTranslation(language, request.getTitle());

        Map<Long, AdminDocumentSection> sectionsById = new HashMap<>();
        for (AdminDocumentSection section : document.getSections()) {
            sectionsById.put(section.getId(), section);
        }

        Set<Long> translatedSectionIds = new HashSet<>();
        List<AdminDocumentTranslationRequest.SectionTranslationRequest> sectionRequests =
            request.getSections() == null ? List.of() : request.getSections();
        for (AdminDocumentTranslationRequest.SectionTranslationRequest sectionRequest : sectionRequests) {
            AdminDocumentSection section = sectionsById.get(sectionRequest.getSectionId());
            if (section == null) {
                throw new BusinessException(ErrorCode.INVALID_ADMIN_DOCUMENT_TRANSLATION);
            }
            if (!translatedSectionIds.add(sectionRequest.getSectionId())) {
                throw new BusinessException(ErrorCode.INVALID_ADMIN_DOCUMENT_TRANSLATION);
            }
            section.upsertTranslation(language, sectionRequest.getSubtitle(), sectionRequest.getContent());
        }

        document.getSections().stream()
            .filter(section -> !translatedSectionIds.contains(section.getId()))
            .forEach(section -> section.removeTranslation(language));
        if (document.isActive() && !document.hasAllRequiredTranslations()) {
            throw new BusinessException(ErrorCode.CANNOT_ACTIVATE_WITHOUT_REQUIRED_TRANSLATIONS);
        }
        return AdminDocumentTranslationsResponse.from(document);
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
                throw new BusinessException(ErrorCode.CANNOT_ACTIVATE_WITHOUT_REQUIRED_TRANSLATIONS);
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
}
