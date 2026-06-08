package com.zimdugo.locker.domain;

public interface LockerVoteStore {
    void save(Long userId, Long lockerId, LockerVoteType voteType);
    void delete(Long userId, Long lockerId);
}
