package com.zimdugo.admin.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminDocumentRepository extends JpaRepository<AdminDocument, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"sections"})
    List<AdminDocument> findByTypeOrderByCreatedAtDesc(DocumentType type);

    List<AdminDocument> findByTypeAndActive(DocumentType type, boolean active);

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"sections"})
    java.util.Optional<AdminDocument> findById(Long id);
}
