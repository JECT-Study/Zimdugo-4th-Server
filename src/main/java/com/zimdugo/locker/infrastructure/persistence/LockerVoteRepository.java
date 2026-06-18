package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.vote.LockerVoteType;
import com.zimdugo.locker.infrastructure.persistence.LockerVoteEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;



public interface LockerVoteRepository extends JpaRepository<LockerVoteEntity, Long> {
    Optional<LockerVoteEntity> findByUserIdAndLockerId(Long userId, Long lockerId);
    boolean existsByUserIdAndLockerIdAndVoteType(Long userId, Long lockerId, LockerVoteType voteType);
    void deleteByUserIdAndLockerId(Long userId, Long lockerId);
}
