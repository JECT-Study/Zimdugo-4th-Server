package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.detail.LockerDetail;
import com.zimdugo.locker.domain.detail.LockerDetailReader;
import com.zimdugo.locker.domain.detail.LockerRealtimeAvailability;
import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.infrastructure.persistence.LockerImageRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.projection.LockerDetailQueryProjection;
import com.zimdugo.locker.infrastructure.projection.LockerSizeTypeQueryProjection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class LockerDetailReaderAdapter implements LockerDetailReader {

    private final LockerRepository lockerRepository;
    private final LockerImageRepository lockerImageRepository;

    @Override
    public Optional<LockerDetail> readById(Long lockerId, Long userId, String languageCode) {
        return lockerRepository.findDetailById(lockerId, userId, languageCode)
            .map(projection -> toDomain(projection, findLockerSizes(List.of(projection.getLockerId()))));
    }

    private LockerDetail toDomain(LockerDetailQueryProjection projection, Set<LockerSizeType> lockerSizes) {
        List<String> imageUrls = findImageUrls(projection);
        return detailBuilder(projection, lockerSizes)
            .imageUrl(imageUrls.isEmpty() ? null : imageUrls.getFirst())
            .imageUrls(imageUrls)
            .isFavorite(Boolean.TRUE.equals(projection.getIsFavorite()))
            .isAccurateVoted(Boolean.TRUE.equals(projection.getIsAccurateVoted()))
            .isInaccurateVoted(Boolean.TRUE.equals(projection.getIsInaccurateVoted()))
            .realtimeAvailability(realtimeAvailability(projection))
            .build();
    }

    private LockerDetail.LockerDetailBuilder detailBuilder(
        LockerDetailQueryProjection projection,
        Set<LockerSizeType> lockerSizes
    ) {
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
            .lockerSizes(lockerSizes)
            .detailInfo(projection.getDetailInfo())
            .startTime(projection.getStartTime()).endTime(projection.getEndTime())
            .accurateVoteCount(projection.getAccurateVoteCount())
            .inaccurateVoteCount(projection.getInaccurateVoteCount())
            .createdAt(projection.getCreatedAt())
            .updatedAt(projection.getUpdatedAt());
    }

    private List<String> findImageUrls(LockerDetailQueryProjection projection) {
        List<String> imageUrls = lockerImageRepository
            .findByLockerIdOrderByListOrderAsc(projection.getLockerId())
            .stream()
            .map(image -> image.getImageUrl())
            .toList();
        return imageUrls.isEmpty() && projection.getImageUrl() != null
            ? List.of(projection.getImageUrl())
            : imageUrls;
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

    private Set<LockerSizeType> findLockerSizes(List<Long> lockerIds) {
        return lockerRepository.findLockerSizeTypesByLockerIds(lockerIds).stream()
            .map(LockerSizeTypeQueryProjection::getSizeType)
            .map(LockerSizeType::from)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(LockerSizeType.class)));
    }
}
