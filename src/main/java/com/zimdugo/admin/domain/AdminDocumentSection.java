package com.zimdugo.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import java.util.ArrayList;
import java.util.List;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admin_document_sections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminDocumentSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String subtitle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int listOrder;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_document_id", nullable = false)
    private AdminDocument adminDocument;

    @OneToMany(mappedBy = "adminDocumentSection", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("languageCode ASC")
    private List<AdminDocumentSectionTranslation> translations = new ArrayList<>();

    @Builder
    public AdminDocumentSection(String subtitle, String content, int listOrder) {
        this.subtitle = subtitle;
        this.content = content;
        this.listOrder = listOrder;
    }

    public void upsertTranslation(String languageCode, String translatedSubtitle, String translatedContent) {
        String normalizedLanguage = DocumentLanguage.normalize(languageCode);
        translations.stream()
            .filter(translation -> translation.getLanguageCode().equals(normalizedLanguage))
            .findFirst()
            .ifPresentOrElse(
                translation -> translation.update(translatedSubtitle, translatedContent),
                () -> translations.add(new AdminDocumentSectionTranslation(
                    this,
                    normalizedLanguage,
                    translatedSubtitle,
                    translatedContent
                ))
            );
    }

    public void removeTranslation(String languageCode) {
        String normalizedLanguage = DocumentLanguage.normalize(languageCode);
        translations.removeIf(translation -> translation.getLanguageCode().equals(normalizedLanguage));
    }

    public String localizedSubtitle(SupportedLanguage language) {
        return requiredTranslation(language).getSubtitle();
    }

    public String localizedContent(SupportedLanguage language) {
        return requiredTranslation(language).getContent();
    }

    public boolean hasTranslation(String languageCode) {
        String normalizedLanguage = DocumentLanguage.normalize(languageCode);
        return translations.stream()
            .anyMatch(translation -> translation.getLanguageCode().equals(normalizedLanguage));
    }

    private AdminDocumentSectionTranslation requiredTranslation(SupportedLanguage language) {
        return translations.stream()
            .filter(translation -> translation.getLanguageCode().equals(language.languageTag()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.I18N_TRANSLATION_MISSING));
    }
}
