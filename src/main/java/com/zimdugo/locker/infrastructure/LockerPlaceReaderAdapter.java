package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.LockerPlace;
import com.zimdugo.locker.domain.LockerPlaceReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockerPlaceReaderAdapter implements LockerPlaceReader {

    private final PlaceRepository placeRepository;

    @Override
    public Optional<LockerPlace> readById(Long placeId, String languageCode) {
        return placeRepository.findPlaceDetailById(placeId, languageCode).map(this::toDomain);
    }

    private LockerPlace toDomain(PlaceDetailQueryProjection projection) {
        return new LockerPlace(
            projection.getPlaceId(),
            projection.getPlaceName(),
            projection.getRoadAddress(),
            projection.getLatitude(),
            projection.getLongitude()
        );
    }
}
