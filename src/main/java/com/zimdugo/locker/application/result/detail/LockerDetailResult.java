package com.zimdugo.locker.application.result.detail;

import com.zimdugo.locker.domain.detail.LockerDetail;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public record LockerDetailResult(
    Long lockerId,
    String lockerName,
    String roadAddress,
    double latitude,
    double longitude,
    long distanceMeters,
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
    LockerRealtimeAvailabilityResult realtimeAvailability
) {
    public static LockerDetailResult from(LockerDetail detail) {
        return resultBuilder(detail).build();
    }

    private static LockerDetailResultBuilder resultBuilder(LockerDetail detail) {
        return LockerDetailResult.builder()
            .lockerId(detail.lockerId())
            .lockerName(detail.lockerName())
            .roadAddress(detail.roadAddress())
            .latitude(detail.latitude()).longitude(detail.longitude()).distanceMeters(detail.distanceMeters())
            .placeId(detail.placeId())
            .placeName(detail.placeName())
            .lockerType(detail.lockerType().name())
            .indoorOutdoorType(detail.indoorOutdoorType().name())
            .groundLevelType(detail.groundLevelType())
            .floor(detail.floor())
            .minPrice(detail.minPrice())
            .maxPrice(detail.maxPrice())
            .lockerSizes(lockerSizes(detail))
            .detailInfo(detail.detailInfo())
            .startTime(detail.startTime())
            .endTime(detail.endTime())
            .imageUrl(detail.imageUrl())
            .imageUrls(imageUrls(detail))
            .accurateVoteCount(detail.accurateVoteCount())
            .inaccurateVoteCount(detail.inaccurateVoteCount())
            .createdAt(detail.createdAt())
            .updatedAt(detail.updatedAt())
            .isFavorite(detail.isFavorite())
            .isAccurateVoted(detail.isAccurateVoted())
            .isInaccurateVoted(detail.isInaccurateVoted())
            .realtimeAvailability(LockerRealtimeAvailabilityResult.from(detail.realtimeAvailability()));
    }

    private static Set<String> lockerSizes(LockerDetail detail) {
        return detail.lockerSizes().stream()
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());
    }

    private static List<String> imageUrls(LockerDetail detail) {
        return detail.imageUrls() == null ? List.of() : detail.imageUrls();
    }
}
