package com.zimdugo.locker.entrypoint.dto.response.detail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zimdugo.locker.application.result.detail.LockerDetailResult;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
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
    List<String> imageUrls,
    int accurateVoteCount,
    int inaccurateVoteCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean isFavorite,
    boolean isAccurateVoted,
    boolean isInaccurateVoted,
    LockerRealtimeAvailabilityResponse realtimeAvailability
) {
    public static LockerDetailResponse from(LockerDetailResult result) {
        return responseBuilder(result).build();
    }

    private static LockerDetailResponseBuilder responseBuilder(LockerDetailResult result) {
        return LockerDetailResponse.builder()
            .lockerId(result.lockerId())
            .lockerName(result.lockerName())
            .roadAddress(result.roadAddress())
            .latitude(result.latitude())
            .longitude(result.longitude())
            .placeId(result.placeId())
            .placeName(result.placeName())
            .lockerType(result.lockerType())
            .indoorOutdoorType(result.indoorOutdoorType())
            .groundLevelType(result.groundLevelType())
            .floor(result.floor())
            .minPrice(result.minPrice())
            .maxPrice(result.maxPrice())
            .lockerSizes(result.lockerSizes())
            .detailInfo(result.detailInfo())
            .startTime(result.startTime())
            .endTime(result.endTime())
            .imageUrl(firstImageUrl(result))
            .imageUrls(imageUrls(result))
            .accurateVoteCount(result.accurateVoteCount())
            .inaccurateVoteCount(result.inaccurateVoteCount())
            .createdAt(result.createdAt())
            .updatedAt(result.updatedAt())
            .isFavorite(result.isFavorite())
            .isAccurateVoted(result.isAccurateVoted())
            .isInaccurateVoted(result.isInaccurateVoted())
            .realtimeAvailability(LockerRealtimeAvailabilityResponse.from(result.realtimeAvailability()));
    }

    private static String firstImageUrl(LockerDetailResult result) {
        return imageUrls(result).isEmpty() ? result.imageUrl() : imageUrls(result).getFirst();
    }

    private static List<String> imageUrls(LockerDetailResult result) {
        return result.imageUrls() == null ? List.of() : result.imageUrls();
    }
}
