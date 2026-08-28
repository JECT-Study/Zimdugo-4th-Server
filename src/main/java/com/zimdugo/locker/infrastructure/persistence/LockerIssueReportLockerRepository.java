package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.infrastructure.projection.LockerIssueReportLockerProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface LockerIssueReportLockerRepository extends Repository<LockerEntity, Long> {

    @Query("""
        SELECT
            l.id AS id,
            l.name AS name,
            l.roadAddress AS roadAddress,
            l.deletedAt AS deletedAt
        FROM LockerEntity l
        WHERE l.id IN :ids
        """)
    List<LockerIssueReportLockerProjection> findLockersByIds(@Param("ids") List<Long> ids);

    @Query("""
        SELECT
            l.id AS id,
            l.name AS name,
            l.roadAddress AS roadAddress,
            l.deletedAt AS deletedAt
        FROM LockerEntity l
        WHERE l.id = :id
        """)
    Optional<LockerIssueReportLockerProjection> findLockerById(@Param("id") Long id);
}
