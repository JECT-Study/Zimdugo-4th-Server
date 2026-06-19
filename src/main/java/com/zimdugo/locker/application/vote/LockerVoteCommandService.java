package com.zimdugo.locker.application.vote;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.detail.LockerDetail;
import com.zimdugo.locker.domain.detail.LockerDetailReader;
import com.zimdugo.locker.domain.vote.LockerVote;
import com.zimdugo.locker.domain.vote.LockerVoteReader;
import com.zimdugo.locker.domain.vote.LockerVoteStore;
import com.zimdugo.locker.domain.vote.LockerVoteType;
import com.zimdugo.locker.domain.detail.LockerDetailStore;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LockerVoteCommandService {

    private final LockerVoteReader lockerVoteReader;
    private final LockerVoteStore lockerVoteStore;
    private final LockerDetailReader lockerDetailReader;
    private final LockerDetailStore lockerDetailStore;

    public void toggleVote(Long userId, Long lockerId, String voteTypeName) {
        LockerVoteType voteType;
        try {
            voteType = LockerVoteType.valueOf(voteTypeName);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }

        LockerDetail lockerDetail = lockerDetailReader.readById(lockerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));

        Optional<LockerVote> existingVote = lockerVoteReader.find(userId, lockerId);

        VoteUpdateResult result = updateVote(userId, lockerId, voteType, lockerDetail, existingVote);
        lockerDetailStore.save(result.lockerDetail());
        log.info(
            "보관함 투표 처리 완료. userId={}, lockerId={}, voteType={}, action={}",
            userId,
            lockerId,
            voteType,
            result.action()
        );
    }

    private VoteUpdateResult updateVote(
        Long userId,
        Long lockerId,
        LockerVoteType voteType,
        LockerDetail lockerDetail,
        Optional<LockerVote> existingVote
    ) {
        if (existingVote.isEmpty()) {
            lockerVoteStore.save(userId, lockerId, voteType);
            return new VoteUpdateResult(lockerDetail.vote(voteType), "CREATE");
        }

        LockerVote vote = existingVote.get();
        if (vote.voteType() == voteType) {
            lockerVoteStore.delete(userId, lockerId);
            return new VoteUpdateResult(lockerDetail.cancelVote(voteType), "CANCEL");
        }

        lockerVoteStore.save(userId, lockerId, voteType);
        return new VoteUpdateResult(lockerDetail.vote(voteType).cancelVote(vote.voteType()), "CHANGE");
    }

    private record VoteUpdateResult(
        LockerDetail lockerDetail,
        String action
    ) {
    }
}
