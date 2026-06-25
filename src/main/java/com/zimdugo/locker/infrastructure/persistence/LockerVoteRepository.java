package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.vote.LockerVoteType;
import com.zimdugo.locker.infrastructure.persistence.LockerVoteEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



public interface LockerVoteRepository extends JpaRepository<LockerVoteEntity, Long> {
    Optional<LockerVoteEntity> findByUserIdAndLockerId(Long userId, Long lockerId);
    boolean existsByUserIdAndLockerIdAndVoteType(Long userId, Long lockerId, LockerVoteType voteType);
    void deleteByUserIdAndLockerId(Long userId, Long lockerId);

    @Modifying
    @Query("DELETE FROM LockerVoteEntity vote WHERE vote.locker.id = :lockerId")
    int deleteByLockerId(@Param("lockerId") Long lockerId);
}
