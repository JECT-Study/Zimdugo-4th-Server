package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.infrastructure.projection.LockerIssueReportLockerProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface LockerIssueReportLockerRepository extends Repository<LockerEntity, Long> {

    @Query(
        value = """
        SELECT
            l.id AS id,
            l.name AS name,
            l.road_address AS roadAddress,
            l.deleted_at AS deletedAt
        FROM lockers l
        WHERE l.id IN :ids
        """,
        nativeQuery = true
    )
    List<LockerIssueReportLockerProjection> findLockersByIds(@Param("ids") List<Long> ids);

    @Query(
        value = """
        SELECT
            l.id AS id,
            l.name AS name,
            l.road_address AS roadAddress,
            l.deleted_at AS deletedAt
        FROM lockers l
        WHERE l.id = :id
        """,
        nativeQuery = true
    )
    Optional<LockerIssueReportLockerProjection> findLockerById(@Param("id") Long id);
}
