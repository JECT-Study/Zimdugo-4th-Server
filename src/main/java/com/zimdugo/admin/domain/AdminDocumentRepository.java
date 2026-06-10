package com.zimdugo.admin.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminDocumentRepository extends JpaRepository<AdminDocument, Long> {
    @EntityGraph(attributePaths = {"sections"})
    List<AdminDocument> findByTypeOrderByListOrderAscCreatedAtDesc(DocumentType type);

    @EntityGraph(attributePaths = {"sections"})
    List<AdminDocument> findByTypeAndActiveOrderByListOrderAscCreatedAtDesc(DocumentType type, boolean active);

    @Override
    @EntityGraph(attributePaths = {"sections"})
    Optional<AdminDocument> findById(Long id);
}
