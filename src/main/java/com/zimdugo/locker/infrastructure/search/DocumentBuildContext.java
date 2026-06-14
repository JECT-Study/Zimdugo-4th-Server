package com.zimdugo.locker.infrastructure.search;

import com.zimdugo.locker.infrastructure.LockerSuggestIndexQueryProjection;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DocumentBuildContext {
    private final LockerSuggestIndexQueryProjection projection;
    private final GeoPoint placeCenter;
    private final int lockerCount;
    private final IndexSyncDataHolder dataHolder;
}
