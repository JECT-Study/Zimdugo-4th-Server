package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.infrastructure.persistence.LockerAliasEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LockerAliasRepository extends JpaRepository<LockerAliasEntity, Long> {

    List<LockerAliasEntity> findByNormalizedAlias(String normalizedAlias);

    List<LockerAliasEntity> findByNormalizedAliasContaining(String normalizedAlias);

    List<LockerAliasEntity> findByLockerId(Long lockerId);

    List<LockerAliasEntity> findByLockerIdIn(List<Long> lockerIds);

    @Modifying
    @Query("DELETE FROM LockerAliasEntity aliasEntity WHERE aliasEntity.locker.id = :lockerId")
    int deleteByLockerId(@Param("lockerId") Long lockerId);
}
