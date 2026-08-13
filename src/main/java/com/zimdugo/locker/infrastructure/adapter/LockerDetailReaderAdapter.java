package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.detail.LockerDetail;
import com.zimdugo.locker.domain.detail.LockerDetailReader;
import com.zimdugo.locker.domain.detail.LockerRealtimeAvailability;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.projection.LockerDetailQueryProjection;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class LockerDetailReaderAdapter implements LockerDetailReader {

    private final LockerRepository lockerRepository;

    @Override
    public Optional<LockerDetail> readById(Long lockerId, Long userId, String languageCode) {
        return lockerRepository.findDetailById(lockerId, userId, languageCode).map(this::toDomain);
    }

    private LockerDetail toDomain(LockerDetailQueryProjection projection) {
        return LockerDetail.builder()
            .lockerId(projection.getLockerId())
            .lockerName(projection.getLockerName())
            .roadAddress(projection.getRoadAddress())
            .latitude(projection.getLatitude())
            .longitude(projection.getLongitude())
            .placeId(projection.getPlaceId())
            .placeName(projection.getPlaceName())
            .lockerType(LockerType.valueOf(projection.getLockerType()))
            .indoorOutdoorType(IndoorOutdoorType.valueOf(projection.getIndoorOutdoorType()))
            .groundLevelType(projection.getGroundLevelType())
            .floor(projection.getFloor())
            .minPrice(projection.getMinPrice())
            .maxPrice(projection.getMaxPrice())
            .lockerSizes(parseLockerSizes(projection.getLockerSizes()))
            .detailInfo(projection.getDetailInfo())
            .startTime(projection.getStartTime())
            .endTime(projection.getEndTime())
            .imageUrl(projection.getImageUrl())
            .accurateVoteCount(projection.getAccurateVoteCount())
            .inaccurateVoteCount(projection.getInaccurateVoteCount())
            .createdAt(projection.getCreatedAt())
            .updatedAt(projection.getUpdatedAt())
            .isFavorite(Boolean.TRUE.equals(projection.getIsFavorite()))
            .isAccurateVoted(Boolean.TRUE.equals(projection.getIsAccurateVoted()))
            .isInaccurateVoted(Boolean.TRUE.equals(projection.getIsInaccurateVoted()))
            .realtimeAvailability(realtimeAvailability(projection))
            .build();
    }

    private LockerRealtimeAvailability realtimeAvailability(LockerDetailQueryProjection projection) {
        if (projection.getRealtimeFetchedAt() == null) {
            return null;
        }
        return new LockerRealtimeAvailability(
            projection.getSmallAvailableCount(),
            projection.getMediumAvailableCount(),
            projection.getLargeAvailableCount(),
            projection.getRealtimeFetchedAt()
        );
    }

    private Set<LockerSizeType> parseLockerSizes(String lockerSizes) {
        if (lockerSizes == null || lockerSizes.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(lockerSizes.split(","))
            .map(LockerSizeType::from)
            .collect(Collectors.toUnmodifiableSet());
    }
}
