package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.locker.domain.place.LockerPlace;
import com.zimdugo.locker.domain.place.LockerPlaceReader;
import com.zimdugo.locker.infrastructure.persistence.PlaceRepository;
import com.zimdugo.locker.infrastructure.projection.PlaceDetailQueryProjection;
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
