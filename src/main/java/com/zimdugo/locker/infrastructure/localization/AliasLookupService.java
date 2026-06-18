package com.zimdugo.locker.infrastructure.localization;

import com.zimdugo.common.i18n.SearchTextNormalizer;
import com.zimdugo.locker.infrastructure.persistence.LockerAliasRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceAliasRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerAliasEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceAliasEntity;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class AliasLookupService {

    private final PlaceAliasRepository placeAliasRepository;
    private final LockerAliasRepository lockerAliasRepository;

    public AliasLookupService(
        PlaceAliasRepository placeAliasRepository,
        LockerAliasRepository lockerAliasRepository
    ) {
        this.placeAliasRepository = placeAliasRepository;
        this.lockerAliasRepository = lockerAliasRepository;
    }

    public List<PlaceAliasEntity> findPlaces(String query) {
        String normalizedQuery = SearchTextNormalizer.normalize(query);
        if (normalizedQuery.isBlank()) {
            return List.of();
        }
        return placeAliasRepository.findByNormalizedAliasContaining(normalizedQuery);
    }

    public List<LockerAliasEntity> findLockers(String query) {
        String normalizedQuery = SearchTextNormalizer.normalize(query);
        if (normalizedQuery.isBlank()) {
            return List.of();
        }
        return lockerAliasRepository.findByNormalizedAliasContaining(normalizedQuery);
    }
}
