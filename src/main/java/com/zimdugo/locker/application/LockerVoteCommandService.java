package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.LockerDetail;
import com.zimdugo.locker.domain.LockerDetailReader;
import com.zimdugo.locker.domain.LockerVote;
import com.zimdugo.locker.domain.LockerVoteReader;
import com.zimdugo.locker.domain.LockerVoteStore;
import com.zimdugo.locker.domain.LockerVoteType;
import com.zimdugo.locker.domain.LockerDetailStore;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new BusinessException(ErrorCode.LOCKER_NOT_FOUND);
        }

        LockerDetail lockerDetail = lockerDetailReader.readById(lockerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));

        Optional<LockerVote> existingVote = lockerVoteReader.find(userId, lockerId);

        if (existingVote.isPresent()) {
            LockerVote vote = existingVote.get();
            if (vote.voteType() == voteType) {
                // 같은 타입 투표 -> 투표 취소
                lockerVoteStore.delete(userId, lockerId);
                lockerDetail = lockerDetail.cancelVote(voteType);
            } else {
                // 다른 타입 투표 -> 투표 타입 변경 (이전 투표 취소 및 새 투표 추가)
                lockerVoteStore.save(userId, lockerId, voteType);
                lockerDetail = lockerDetail.vote(voteType).cancelVote(vote.voteType());
            }
        } else {
            // 투표 없음 -> 신규 투표 생성
            lockerVoteStore.save(userId, lockerId, voteType);
            lockerDetail = lockerDetail.vote(voteType);
        }

        lockerDetailStore.save(lockerDetail);
    }
}
