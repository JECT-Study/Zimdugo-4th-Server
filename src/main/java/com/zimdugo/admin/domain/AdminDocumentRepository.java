package com.zimdugo.admin.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminDocumentRepository extends JpaRepository<AdminDocument, Long> {
    long countByType(DocumentType type);

    long countByTypeAndActive(DocumentType type, boolean active);

    @Query("select d from AdminDocument d left join fetch d.sections "
        + "where d.type = :type order by d.listOrder asc, d.createdAt desc")
    List<AdminDocument> findByType(@Param("type") DocumentType type);

    @Query("select d from AdminDocument d left join fetch d.sections "
        + "where d.type = :type and d.active = :active order by d.listOrder asc, d.createdAt desc")
    List<AdminDocument> findByTypeAndActive(@Param("type") DocumentType type, @Param("active") boolean active);

    @Override
    @Query("select d from AdminDocument d left join fetch d.sections where d.id = :id")
    Optional<AdminDocument> findById(@Param("id") Long id);
}
