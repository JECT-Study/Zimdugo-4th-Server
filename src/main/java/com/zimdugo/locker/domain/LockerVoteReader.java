package com.zimdugo.locker.domain;

import java.util.Optional;

public interface LockerVoteReader {
    Optional<LockerVote> find(Long userId, Long lockerId);
    boolean exists(Long userId, Long lockerId, LockerVoteType voteType);
}
