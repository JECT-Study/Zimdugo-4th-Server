package com.zimdugo.locker.domain;

import java.time.LocalDateTime;

public record LockerVote(
    Long id,
    Long userId,
    Long lockerId,
    LockerVoteType voteType,
    LocalDateTime createdAt
) {
}
