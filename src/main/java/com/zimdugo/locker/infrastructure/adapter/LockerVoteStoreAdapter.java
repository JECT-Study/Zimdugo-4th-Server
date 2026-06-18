package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.vote.LockerVoteStore;
import com.zimdugo.locker.domain.vote.LockerVoteType;
import com.zimdugo.locker.infrastructure.persistence.LockerEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerVoteEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerVoteRepository;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import com.zimdugo.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class LockerVoteStoreAdapter implements LockerVoteStore {

    private final LockerVoteRepository lockerVoteRepository;
    private final UserRepository userRepository;
    private final LockerRepository lockerRepository;

    @Override
    public void save(Long userId, Long lockerId, LockerVoteType voteType) {
        lockerVoteRepository.findByUserIdAndLockerId(userId, lockerId)
            .ifPresentOrElse(
                existing -> existing.changeVoteType(voteType),
                () -> saveNewVote(userId, lockerId, voteType)
            );
    }

    private void saveNewVote(Long userId, Long lockerId, LockerVoteType voteType) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.AUTHENTICATED_USER_NOT_FOUND));
        LockerEntity locker = lockerRepository.findById(lockerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));

        LockerVoteEntity voteEntity = new LockerVoteEntity(user, locker, voteType);
        lockerVoteRepository.save(voteEntity);
    }

    @Override
    public void delete(Long userId, Long lockerId) {
        lockerVoteRepository.deleteByUserIdAndLockerId(userId, lockerId);
    }
}
