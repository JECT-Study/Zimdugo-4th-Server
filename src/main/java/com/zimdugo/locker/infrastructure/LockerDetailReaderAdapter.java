package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.IndoorOutdoorType;
import com.zimdugo.locker.domain.LockerDetail;
import com.zimdugo.locker.domain.LockerDetailReader;
import com.zimdugo.locker.domain.LockerSizeType;
import com.zimdugo.locker.domain.LockerType;
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
        return new LockerDetail(
            projection.getLockerId(),
            projection.getLockerName(),
            projection.getRoadAddress(),
            projection.getLatitude(),
            projection.getLongitude(),
            projection.getPlaceId(),
            projection.getPlaceName(),
            LockerType.valueOf(projection.getLockerType()),
            IndoorOutdoorType.valueOf(projection.getIndoorOutdoorType()),
            projection.getGroundLevelType(),
            projection.getFloor(),
            projection.getMinPrice(),
            projection.getMaxPrice(),
            parseLockerSizes(projection.getLockerSizes()),
            projection.getDetailInfo(),
            projection.getStartTime(),
            projection.getEndTime(),
            projection.getImageUrl(),
            projection.getAccurateVoteCount(),
            projection.getInaccurateVoteCount(),
            projection.getCreatedAt(),
            projection.getUpdatedAt(),
            Boolean.TRUE.equals(projection.getIsFavorite()),
            Boolean.TRUE.equals(projection.getIsAccurateVoted()),
            Boolean.TRUE.equals(projection.getIsInaccurateVoted())
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
