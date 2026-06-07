package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.LockerPlace;
import com.zimdugo.locker.domain.LockerPlaceReader;
import com.zimdugo.locker.infrastructure.persistence.PlaceEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockerPlaceReaderAdapter implements LockerPlaceReader {

    private final PlaceRepository placeRepository;

    @Override
    public Optional<LockerPlace> readById(Long placeId) {
        return placeRepository.findById(placeId).map(this::toDomain);
    }

    private LockerPlace toDomain(PlaceEntity place) {
        return new LockerPlace(
            place.getId(),
            place.getName(),
            place.getRoadAddress(),
            place.getLatitude(),
            place.getLongitude()
        );
    }
}
