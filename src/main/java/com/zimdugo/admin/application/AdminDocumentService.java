package com.zimdugo.admin.application;

import com.zimdugo.admin.domain.AdminDocument;
import com.zimdugo.admin.domain.AdminDocumentRepository;
import com.zimdugo.admin.domain.AdminDocumentSection;
import com.zimdugo.admin.domain.DocumentType;
import com.zimdugo.admin.ui.dto.AdminDocumentForm;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDocumentService {

    private final AdminDocumentRepository adminDocumentRepository;

    public List<AdminDocument> getDocumentsByType(DocumentType type) {
        return adminDocumentRepository.findByType(type);
    }

    public List<AdminDocument> getActiveDocumentsByType(DocumentType type) {
        return adminDocumentRepository.findByTypeAndActive(type, true);
    }

    public AdminDocument getById(Long id) {
        return adminDocumentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자 문서입니다. ID: " + id));
    }

    @Transactional
    public AdminDocument createDocument(AdminDocumentForm form) {
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
        
        document.update(form.getTitle(), newSections);
        return document;
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
                throw new IllegalArgumentException("공지사항 타입의 문서만 순서를 변경할 수 있습니다. ID: " + document.getId());
            }
            document.updateListOrder(i);
        }
    }
}
