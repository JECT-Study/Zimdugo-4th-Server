package com.zimdugo.locker.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import lombok.Builder;

@Builder(toBuilder = true)
public record LockerDetail(
    Long lockerId,
    String lockerName,
    String roadAddress,
    double latitude,
    double longitude,
    Long placeId,
    String placeName,
    LockerType lockerType,
    IndoorOutdoorType indoorOutdoorType,
    String groundLevelType,
    Integer floor,
    Integer minPrice,
    Integer maxPrice,
    Set<LockerSizeType> lockerSizes,
    String detailInfo,
    LocalTime startTime,
    LocalTime endTime,
    String imageUrl,
    int accurateVoteCount,
    int inaccurateVoteCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public LockerDetail voteCorrect() {
        return this.toBuilder()
            .accurateVoteCount(accurateVoteCount + 1)
            .build();
    }

    public LockerDetail cancelVoteCorrect() {
        return this.toBuilder()
            .accurateVoteCount(Math.max(0, accurateVoteCount - 1))
            .build();
    }

    public LockerDetail voteIncorrect() {
        return this.toBuilder()
            .inaccurateVoteCount(inaccurateVoteCount + 1)
            .build();
    }

    public LockerDetail cancelVoteIncorrect() {
        return this.toBuilder()
            .inaccurateVoteCount(Math.max(0, inaccurateVoteCount - 1))
            .build();
    }

    public LockerDetail vote(LockerVoteType voteType) {
        return voteType == LockerVoteType.CORRECT ? voteCorrect() : voteIncorrect();
    }

    public LockerDetail cancelVote(LockerVoteType voteType) {
        return voteType == LockerVoteType.CORRECT ? cancelVoteCorrect() : cancelVoteIncorrect();
    }
}
