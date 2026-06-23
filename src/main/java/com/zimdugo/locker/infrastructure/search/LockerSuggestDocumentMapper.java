package com.zimdugo.locker.infrastructure.search;

import com.zimdugo.common.i18n.SearchTextNormalizer;
import com.zimdugo.common.util.HangulUtils;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.infrastructure.persistence.LockerAliasEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceAliasEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceTranslationEntity;
import com.zimdugo.locker.infrastructure.projection.LockerSuggestIndexQueryProjection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

public final class LockerSuggestDocumentMapper {

    private LockerSuggestDocumentMapper() {
    }

    public static List<LockerSuggestDocument> toDocuments(
        List<LockerSuggestIndexQueryProjection> projections,
        IndexSyncDataHolder holder
    ) {
        if (projections.isEmpty()) {
            return List.of();
        }

        Map<Long, List<LockerSuggestIndexQueryProjection>> projectionsByPlace = groupByPlace(projections);
        Map<Long, GeoPoint> centerByPlace = calculateCentroids(projectionsByPlace);

        List<LockerSuggestDocument> documents = new ArrayList<>(projections.size());
        for (Map.Entry<Long, List<LockerSuggestIndexQueryProjection>> entry : projectionsByPlace.entrySet()) {
            Long placeId = entry.getKey();
            List<LockerSuggestIndexQueryProjection> placeProjections = entry.getValue();
            GeoPoint center = centerByPlace.get(placeId);
            int lockerCount = placeProjections.size();

            for (LockerSuggestIndexQueryProjection projection : placeProjections) {
                DocumentBuildContext ctx = new DocumentBuildContext(
                    projection,
                    center,
                    lockerCount,
                    holder
                );
                documents.add(buildDocument(ctx));
            }
        }
        return documents;
    }

    private static Map<Long, List<LockerSuggestIndexQueryProjection>> groupByPlace(
        List<LockerSuggestIndexQueryProjection> projections
    ) {
        Map<Long, List<LockerSuggestIndexQueryProjection>> map = new HashMap<>();
        for (LockerSuggestIndexQueryProjection p : projections) {
            map.computeIfAbsent(p.getPlaceId(), k -> new ArrayList<>()).add(p);
        }
        return map;
    }

    private static Map<Long, GeoPoint> calculateCentroids(Map<Long, List<LockerSuggestIndexQueryProjection>> groups) {
        Map<Long, GeoPoint> centroids = new HashMap<>();
        for (Map.Entry<Long, List<LockerSuggestIndexQueryProjection>> entry : groups.entrySet()) {
            double sumLat = 0;
            double sumLon = 0;
            for (LockerSuggestIndexQueryProjection p : entry.getValue()) {
                sumLat += p.getLockerLatitude();
                sumLon += p.getLockerLongitude();
            }
            int count = entry.getValue().size();
            centroids.put(entry.getKey(), new GeoPoint(sumLat / count, sumLon / count));
        }
        return centroids;
    }

    private static List<String> buildPlaceSearchNames(
        LockerSuggestIndexQueryProjection p,
        List<PlaceTranslationEntity> pTrans,
        List<PlaceAliasEntity> pAliases
    ) {
        Set<String> names = new LinkedHashSet<>();
        if (p.getPlaceName() != null) {
            names.add(p.getPlaceName());
        }
        pTrans.forEach(pt -> {
            if (pt.getName() != null) {
                names.add(pt.getName());
            }
        });
        pAliases.forEach(pa -> {
            if (pa.getAlias() != null) {
                names.add(pa.getAlias());
            }
            if (pa.getNormalizedAlias() != null) {
                names.add(pa.getNormalizedAlias());
            }
        });
        return normalizeAndFilter(names);
    }

    private static List<String> buildLockerSearchNames(
        LockerSuggestIndexQueryProjection p,
        List<LockerTranslationEntity> lTrans,
        List<LockerAliasEntity> lAliases
    ) {
        Set<String> names = new LinkedHashSet<>();
        if (p.getLockerName() != null) {
            names.add(p.getLockerName());
        }
        lTrans.forEach(lt -> {
            if (lt.getName() != null) {
                names.add(lt.getName());
            }
        });
        lAliases.forEach(la -> {
            if (la.getAlias() != null) {
                names.add(la.getAlias());
            }
            if (la.getNormalizedAlias() != null) {
                names.add(la.getNormalizedAlias());
            }
        });
        return normalizeAndFilter(names);
    }

    private static List<String> buildSearchAddresses(
        LockerSuggestIndexQueryProjection p,
        List<PlaceTranslationEntity> pTrans,
        List<LockerTranslationEntity> lTrans
    ) {
        Set<String> addrs = new LinkedHashSet<>();
        if (p.getRoadAddress() != null) {
            addrs.add(p.getRoadAddress());
        }
        if (p.getPlaceRoadAddress() != null) {
            addrs.add(p.getPlaceRoadAddress());
        }
        pTrans.forEach(pt -> {
            if (pt.getRoadAddress() != null) {
                addrs.add(pt.getRoadAddress());
            }
        });
        lTrans.forEach(lt -> {
            if (lt.getRoadAddress() != null) {
                addrs.add(lt.getRoadAddress());
            }
        });
        return normalizeAndFilter(addrs);
    }

    private static Map<String, String> buildLocalizedLockerNames(List<LockerTranslationEntity> lTrans) {
        return lTrans.stream()
            .filter(lt -> lt.getName() != null && lt.getLanguage() != null)
            .collect(Collectors.toMap(
                lt -> lt.getLanguage().name().toLowerCase(),
                LockerTranslationEntity::getName,
                (v1, v2) -> v1
            ));
    }

    private static Map<String, String> buildLocalizedRoadAddresses(List<LockerTranslationEntity> lTrans) {
        return lTrans.stream()
            .filter(lt -> lt.getRoadAddress() != null && lt.getLanguage() != null)
            .collect(Collectors.toMap(
                lt -> lt.getLanguage().name().toLowerCase(),
                LockerTranslationEntity::getRoadAddress,
                (v1, v2) -> v1
            ));
    }

    private static Map<String, String> buildLocalizedPlaceNames(List<PlaceTranslationEntity> pTrans) {
        return pTrans.stream()
            .filter(pt -> pt.getName() != null && pt.getLanguage() != null)
            .collect(Collectors.toMap(
                pt -> pt.getLanguage().name().toLowerCase(),
                PlaceTranslationEntity::getName,
                (v1, v2) -> v1
            ));
    }

    private static LockerSuggestDocument buildDocument(DocumentBuildContext ctx) {
        LockerSuggestIndexQueryProjection p = ctx.getProjection();
        IndexSyncDataHolder dh = ctx.getDataHolder();
        List<String> pNames = buildPlaceSearchNames(
            p, dh.getPlaceTranslations().getOrDefault(p.getPlaceId(), List.of()),
            dh.getPlaceAliases().getOrDefault(p.getPlaceId(), List.of())
        );
        List<String> lNames = buildLockerSearchNames(
            p, dh.getLockerTranslations().getOrDefault(p.getLockerId(), List.of()),
            dh.getLockerAliases().getOrDefault(p.getLockerId(), List.of())
        );
        List<String> addrs = buildSearchAddresses(
            p, dh.getPlaceTranslations().getOrDefault(p.getPlaceId(), List.of()),
            dh.getLockerTranslations().getOrDefault(p.getLockerId(), List.of())
        );

        LockerSuggestDocument.LockerSuggestDocumentBuilder builder = LockerSuggestDocument.builder()
            .id(String.valueOf(p.getLockerId())).lockerId(p.getLockerId())
            .lockerName(p.getLockerName()).lockerNameDecomposed(HangulUtils.decompose(p.getLockerName()))
            .lockerSearchNames(lNames).lockerSearchNamesDecomposed(decompose(lNames))
            .roadAddress(p.getRoadAddress()).roadAddressDecomposed(HangulUtils.decompose(p.getRoadAddress()))
            .searchAddresses(addrs).searchAddressesDecomposed(decompose(addrs))
            .lockerType(p.getLockerType()).indoorOutdoorType(p.getIndoorOutdoorType())
            .lockerSize(parseLockerSizes(p.getLockerSize()))
            .minPrice(p.getMinPrice()).updatedAt(p.getUpdatedAt());

        return fillPlaceAndTranslations(builder, ctx, pNames);
    }

    private static LockerSuggestDocument fillPlaceAndTranslations(
        LockerSuggestDocument.LockerSuggestDocumentBuilder builder,
        DocumentBuildContext ctx,
        List<String> pNames
    ) {
        LockerSuggestIndexQueryProjection p = ctx.getProjection();
        IndexSyncDataHolder dh = ctx.getDataHolder();
        return builder
            .placeId(p.getPlaceId())
            .placeName(p.getPlaceName())
            .placeNameDecomposed(HangulUtils.decompose(p.getPlaceName()))
            .placeSearchNames(pNames)
            .placeSearchNamesDecomposed(decompose(pNames))
            .location(new GeoPoint(p.getLockerLatitude(), p.getLockerLongitude()))
            .placeLocation(ctx.getPlaceCenter())
            .lockerCount(ctx.getLockerCount())
            .localizedLockerNames(buildLocalizedLockerNames(
                dh.getLockerTranslations().getOrDefault(p.getLockerId(), List.of())
            ))
            .localizedPlaceNames(buildLocalizedPlaceNames(
                dh.getPlaceTranslations().getOrDefault(p.getPlaceId(), List.of())
            ))
            .localizedRoadAddresses(buildLocalizedRoadAddresses(
                dh.getLockerTranslations().getOrDefault(p.getLockerId(), List.of())
            ))
            .build();
    }

    private static List<String> normalizeAndFilter(Collection<String> values) {
        return values.stream()
            .map(SearchTextNormalizer::normalize)
            .filter(value -> value != null && !value.isBlank())
            .distinct()
            .toList();
    }

    private static List<String> decompose(List<String> values) {
        return values.stream().map(HangulUtils::decompose).toList();
    }

    private static List<String> parseLockerSizes(String lockerSizes) {
        if (lockerSizes == null || lockerSizes.isBlank()) {
            return List.of();
        }
        return Arrays.stream(lockerSizes.split(","))
            .map(LockerSizeType::from)
            .filter(Objects::nonNull)
            .map(Enum::name)
            .distinct()
            .toList();
    }
}
