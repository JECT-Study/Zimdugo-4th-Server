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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentType type;

    @Column(nullable = false, length = 255)
    private String title;

    @OneToMany(mappedBy = "adminDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("listOrder ASC")
    private List<AdminDocumentSection> sections = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean active = false;

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

    public void update(String title, List<AdminDocumentSection> newSections) {
        this.title = title;
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

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
