package com.zimdugo.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "admin_document_section_translations",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_admin_document_section_translation_language",
        columnNames = {"admin_document_section_id", "language_code"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminDocumentSectionTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_document_section_id", nullable = false)
    private AdminDocumentSection adminDocumentSection;

    @Column(name = "language_code", nullable = false, length = 35)
    private String languageCode;

    @Column(length = 255)
    private String subtitle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    AdminDocumentSectionTranslation(
        AdminDocumentSection adminDocumentSection,
        String languageCode,
        String subtitle,
        String content
    ) {
        this.adminDocumentSection = adminDocumentSection;
        this.languageCode = DocumentLanguage.normalize(languageCode);
        this.subtitle = subtitle;
        this.content = content;
    }

    void update(String subtitle, String content) {
        this.subtitle = subtitle;
        this.content = content;
    }
}
