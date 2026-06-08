package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.LockerVote;
import com.zimdugo.locker.domain.LockerVoteReader;
import com.zimdugo.locker.domain.LockerVoteType;
import com.zimdugo.locker.infrastructure.persistence.LockerVoteEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockerVoteReaderAdapter implements LockerVoteReader {

    private final LockerVoteRepository lockerVoteRepository;

    @Override
    public Optional<LockerVote> find(Long userId, Long lockerId) {
        return lockerVoteRepository.findByUserIdAndLockerId(userId, lockerId)
            .map(this::toDomain);
    }

    @Override
    public boolean exists(Long userId, Long lockerId, LockerVoteType voteType) {
        return lockerVoteRepository.existsByUserIdAndLockerIdAndVoteType(userId, lockerId, voteType);
    }

    private LockerVote toDomain(LockerVoteEntity entity) {
        return new LockerVote(
            entity.getId(),
            entity.getUser().getId(),
            entity.getLocker().getId(),
            entity.getVoteType(),
            entity.getCreatedAt()
        );
    }
}
