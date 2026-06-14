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
    name = "admin_document_translations",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_admin_document_translation_language",
        columnNames = {"admin_document_id", "language_code"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminDocumentTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_document_id", nullable = false)
    private AdminDocument adminDocument;

    @Column(name = "language_code", nullable = false, length = 35)
    private String languageCode;

    @Column(nullable = false, length = 255)
    private String title;

    AdminDocumentTranslation(AdminDocument adminDocument, String languageCode, String title) {
        this.adminDocument = adminDocument;
        this.languageCode = DocumentLanguage.normalize(languageCode);
        this.title = title;
    }

    void updateTitle(String title) {
        this.title = title;
    }
}
