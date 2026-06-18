package com.zimdugo.locker.application.result.detail;

import com.zimdugo.locker.domain.detail.LockerDetail;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

public record LockerDetailResult(
    Long lockerId,
    String lockerName,
    String roadAddress,
    double latitude,
    double longitude,
    Long placeId,
    String placeName,
    String lockerType,
    String indoorOutdoorType,
    String groundLevelType,
    Integer floor,
    Integer minPrice,
    Integer maxPrice,
    Set<String> lockerSizes,
    String detailInfo,
    LocalTime startTime,
    LocalTime endTime,
    String imageUrl,
    int accurateVoteCount,
    int inaccurateVoteCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean isFavorite,
    boolean isAccurateVoted,
    boolean isInaccurateVoted
) {
    public static LockerDetailResult from(LockerDetail detail) {
        Set<String> sizes = detail.lockerSizes().stream()
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

        return new LockerDetailResult(
            detail.lockerId(), detail.lockerName(), detail.roadAddress(),
            detail.latitude(), detail.longitude(), detail.placeId(), detail.placeName(),
            detail.lockerType().name(), detail.indoorOutdoorType().name(), detail.groundLevelType(),
            detail.floor(), detail.minPrice(), detail.maxPrice(), sizes, detail.detailInfo(),
            detail.startTime(), detail.endTime(), detail.imageUrl(),
            detail.accurateVoteCount(), detail.inaccurateVoteCount(),
            detail.createdAt(), detail.updatedAt(),
            detail.isFavorite(), detail.isAccurateVoted(), detail.isInaccurateVoted()
        );
    }
}
