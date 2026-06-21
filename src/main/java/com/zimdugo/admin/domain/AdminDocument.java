package com.zimdugo.admin.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.common.i18n.SupportedLanguage;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminDocument {

    public static final int MAX_NOTICE_IMAGE_COUNT = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 500)
    private String imageUrl;

    @OneToMany(mappedBy = "adminDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("listOrder ASC")
    private List<AdminDocumentImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "adminDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("listOrder ASC")
    private List<AdminDocumentSection> sections = new ArrayList<>();

    @OneToMany(mappedBy = "adminDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("languageCode ASC")
    private List<AdminDocumentTranslation> translations = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean active = false;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int listOrder = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime appliedAt;

    @Builder
    public AdminDocument(
        DocumentType type,
        String title,
        List<AdminDocumentSection> sections,
        boolean active,
        LocalDateTime appliedAt
    ) {
        this.type = type;
        this.title = title;
        this.active = active;
        this.appliedAt = appliedAt;
        if (sections != null) {
            sections.forEach(this::addSection);
        }
    }

    public void activate() {
        this.active = true;
        this.appliedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
    }

    public void update(String title, String imageUrl, List<AdminDocumentSection> newSections) {
        update(title, imageUrl == null ? List.of() : List.of(imageUrl), newSections);
    }

    public void update(String title, List<String> imageUrls, List<AdminDocumentSection> newSections) {
        this.title = title;
        replaceImages(imageUrls);
        this.sections.clear();
        if (newSections != null) {
            newSections.forEach(this::addSection);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void addSection(AdminDocumentSection section) {
        this.sections.add(section);
        section.setAdminDocument(this);
    }

    public void upsertTranslation(String languageCode, String translatedTitle) {
        String normalizedLanguage = DocumentLanguage.normalize(languageCode);
        translations.stream()
            .filter(translation -> translation.getLanguageCode().equals(normalizedLanguage))
            .findFirst()
            .ifPresentOrElse(
                translation -> translation.updateTitle(translatedTitle),
                () -> translations.add(new AdminDocumentTranslation(this, normalizedLanguage, translatedTitle))
            );
    }

    public String localizedTitle(SupportedLanguage language) {
        return translations.stream()
            .filter(translation -> translation.getLanguageCode().equals(language.languageTag()))
            .map(AdminDocumentTranslation::getTitle)
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.I18N_TRANSLATION_MISSING));
    }

    public boolean hasCompleteTranslation(String languageCode) {
        String normalizedLanguage = DocumentLanguage.normalize(languageCode);
        boolean titleTranslated = translations.stream()
            .anyMatch(translation -> translation.getLanguageCode().equals(normalizedLanguage));
        return titleTranslated && sections.stream().allMatch(section -> section.hasTranslation(normalizedLanguage));
    }

    public boolean hasAllRequiredTranslations() {
        return SupportedLanguage.all().stream()
            .allMatch(language -> hasCompleteTranslation(language.languageTag()));
    }

    public void updateListOrder(int listOrder) {
        this.listOrder = listOrder;
    }

    public void updateImageUrl(String imageUrl) {
        String normalizedImageUrl = normalizeImageUrl(imageUrl);
        if (normalizedImageUrl == null) {
            replaceImages(List.of());
            return;
        }
        replaceImages(List.of(normalizedImageUrl));
    }

    public void replaceImages(List<String> imageUrls) {
        List<String> normalizedImageUrls = imageUrls == null ? List.of() : imageUrls.stream()
            .map(this::normalizeImageUrl)
            .filter(java.util.Objects::nonNull)
            .toList();
        if (normalizedImageUrls.size() > MAX_NOTICE_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.TOO_MANY_NOTICE_IMAGES);
        }

        images.clear();
        for (int index = 0; index < normalizedImageUrls.size(); index++) {
            images.add(new AdminDocumentImage(this, normalizedImageUrls.get(index), index));
        }
        imageUrl = normalizedImageUrls.isEmpty() ? null : normalizedImageUrls.getFirst();
    }

    public List<String> getImageUrls() {
        if (!images.isEmpty()) {
            return images.stream()
                .map(AdminDocumentImage::getImageUrl)
                .toList();
        }
        String normalizedImageUrl = normalizeImageUrl(imageUrl);
        return normalizedImageUrl == null ? List.of() : List.of(normalizedImageUrl);
    }

    private String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        return imageUrl.trim();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
