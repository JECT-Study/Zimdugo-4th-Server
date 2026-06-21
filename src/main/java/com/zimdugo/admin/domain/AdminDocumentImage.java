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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin_document_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminDocumentImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private int listOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_document_id", nullable = false)
    private AdminDocument adminDocument;

    AdminDocumentImage(AdminDocument adminDocument, String imageUrl, int listOrder) {
        this.adminDocument = adminDocument;
        this.imageUrl = imageUrl;
        this.listOrder = listOrder;
    }
}
