package com.zimdugo.locker.infrastructure.search;

import com.zimdugo.locker.infrastructure.persistence.LockerAliasEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceAliasEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceTranslationEntity;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IndexSyncDataHolder {
    private final Map<Long, List<LockerTranslationEntity>> lockerTranslations;
    private final Map<Long, List<LockerAliasEntity>> lockerAliases;
    private final Map<Long, List<PlaceTranslationEntity>> placeTranslations;
    private final Map<Long, List<PlaceAliasEntity>> placeAliases;
}
