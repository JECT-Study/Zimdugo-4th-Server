package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LockerReportRepository extends JpaRepository<LockerReportEntity, Long> {

    @EntityGraph(attributePaths = "locker")
    Page<LockerReportEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("""
        SELECT lr.locker.id AS lockerId, MAX(lr.updatedAt) AS lastCompletedVoteAt
        FROM LockerReportEntity lr
        WHERE lr.locker.id IN :lockerIds
          AND lr.status = com.zimdugo.locker.domain.LockerReportStatus.COMPLETED
        GROUP BY lr.locker.id
        """)
    List<LockerReportLatestUpdateProjection> findLatestCompletedVoteAtByLockerIdIn(
        @Param("lockerIds") Collection<Long> lockerIds
    );
}
