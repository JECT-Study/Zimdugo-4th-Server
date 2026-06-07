package com.zimdugo.locker.entrypoint.dto.response.detail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zimdugo.locker.application.result.detail.LockerDetailResult;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LockerDetailResponse(
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
    boolean isFavorite
) {
    public static LockerDetailResponse from(LockerDetailResult result) {
        return new LockerDetailResponse(
            result.lockerId(),
            result.lockerName(),
            result.roadAddress(),
            result.latitude(),
            result.longitude(),
            result.placeId(),
            result.placeName(),
            result.lockerType(),
            result.indoorOutdoorType(),
            result.groundLevelType(),
            result.floor(),
            result.minPrice(),
            result.maxPrice(),
            result.lockerSizes(),
            result.detailInfo(),
            result.startTime(),
            result.endTime(),
            result.imageUrl(),
            result.accurateVoteCount(),
            result.inaccurateVoteCount(),
            result.createdAt(),
            result.updatedAt(),
            result.isFavorite()
        );
    }
}
