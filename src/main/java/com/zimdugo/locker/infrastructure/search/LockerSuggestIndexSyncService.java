package com.zimdugo.locker.infrastructure.search;

import com.zimdugo.common.i18n.SearchTextNormalizer;
import com.zimdugo.common.util.HangulUtils;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.LockerSizeType;
import com.zimdugo.locker.infrastructure.LockerRepository;
import com.zimdugo.locker.infrastructure.LockerAliasRepository;
import com.zimdugo.locker.infrastructure.PlaceAliasRepository;
import com.zimdugo.locker.infrastructure.LockerTranslationRepository;
import com.zimdugo.locker.infrastructure.PlaceTranslationRepository;
import com.zimdugo.locker.infrastructure.LockerSuggestIndexQueryProjection;
import com.zimdugo.locker.infrastructure.persistence.LockerAliasEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceAliasEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceTranslationEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.index.AliasData;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockerSuggestIndexSyncService {

    private static final String INDEX_ALIAS = "locker_suggest";
    private static final String VERSIONED_INDEX_PREFIX = INDEX_ALIAS + "_v_";

    private final LockerRepository lockerRepository;
    private final LockerSuggestSearchRepository lockerSuggestSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final LockerAliasRepository lockerAliasRepository;
    private final PlaceAliasRepository placeAliasRepository;
    private final LockerTranslationRepository lockerTranslationRepository;
    private final PlaceTranslationRepository placeTranslationRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void syncAtStartup() {
        String targetIndex = newVersionedIndexName();
        log.info("보관함 검색 인덱스 동기화 시작...");
        try {
            List<LockerSuggestDocument> documents = toDocuments(lockerRepository.findAllForSuggestIndex());
            rebuildAndSwitchAlias(targetIndex, documents);
        } catch (DataAccessException e) {
            deleteIndexQuietly(targetIndex);
            log.error("검색 인덱스 동기화 실패: DB 조회 중 오류 발생 [사유: {}]", e.getMessage());
            throw new BusinessException(ErrorCode.INDEX_SYNC_FAILED, e);
        } catch (Exception e) {
            deleteIndexQuietly(targetIndex);
            log.error("검색 인덱스 동기화 실패: 알 수 없는 오류 [종류: {}, 사유: {}]",
                e.getClass().getSimpleName(), e.getMessage());
            throw new BusinessException(ErrorCode.INDEX_SYNC_FAILED, e);
        }
    }

    @Transactional(readOnly = true)
    public void reindexPlace(Long placeId) {
        reindexPlaces(placeId == null ? List.of() : List.of(placeId));
    }

    @Transactional(readOnly = true)
    public void reindexPlaces(Collection<Long> placeIds) {
        List<Long> targets = distinctIds(placeIds);
        if (targets.isEmpty()) {
            return;
        }

        ensureAlias();
        lockerSuggestSearchRepository.deleteByPlaceIdIn(targets);
        saveDocuments(toDocuments(lockerRepository.findAllForSuggestIndexByPlaceIds(targets)));
    }

    @Transactional(readOnly = true)
    public void reindexLocker(Long lockerId) {
        reindexLockers(lockerId == null ? List.of() : List.of(lockerId));
    }

    @Transactional(readOnly = true)
    public void reindexLockers(Collection<Long> lockerIds) {
        List<Long> targets = distinctIds(lockerIds);
        if (targets.isEmpty()) {
            return;
        }

        lockerSuggestSearchRepository.deleteAllById(targets.stream().map(String::valueOf).toList());
        reindexPlaces(lockerRepository.findPlaceIdsByLockerIds(targets));
    }

    private void ensureAlias() {
        IndexOperations aliasIndexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(INDEX_ALIAS));
        if (!aliasIndexOperations.exists() || !hasWriteAlias(aliasIndexOperations)) {
            throw new BusinessException(ErrorCode.INDEX_SYNC_FAILED);
        }
    }

    private void saveDocuments(List<LockerSuggestDocument> documents) {
        if (documents.isEmpty()) {
            log.warn("동기화할 보관함 데이터가 없습니다.");
            return;
        }
        lockerSuggestSearchRepository.saveAll(documents);
        log.info("보관함 검색 인덱스 동기화 완료: {}건", documents.size());
    }

    private void rebuildAndSwitchAlias(String targetIndex, List<LockerSuggestDocument> documents) {
        IndexOperations entityIndex = elasticsearchOperations.indexOps(LockerSuggestDocument.class);
        IndexOperations targetIndexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(targetIndex));
        if (!targetIndexOperations.create(entityIndex.createSettings(), entityIndex.createMapping())) {
            throw new BusinessException(ErrorCode.INDEX_SYNC_FAILED);
        }
        if (!documents.isEmpty()) {
            elasticsearchOperations.save(documents, IndexCoordinates.of(targetIndex));
        }
        targetIndexOperations.refresh();
        switchAlias(targetIndex, targetIndexOperations);
        log.info("검색 인덱스 alias 전환 완료: {} -> {}", INDEX_ALIAS, targetIndex);
    }

    private void switchAlias(String targetIndex, IndexOperations targetIndexOperations) {
        IndexOperations aliasIndexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(INDEX_ALIAS));
        boolean logicalIndexExists = aliasIndexOperations.exists();
        Set<String> previousIndices = logicalIndexExists ? aliasIndices(aliasIndexOperations) : Set.of();
        AliasActions actions = new AliasActions();
        if (logicalIndexExists && previousIndices.isEmpty()) {
            actions.add(new AliasAction.RemoveIndex(aliasParameters(INDEX_ALIAS, null)));
        } else {
            previousIndices.forEach(index ->
                actions.add(new AliasAction.Remove(aliasParameters(index, INDEX_ALIAS)))
            );
        }
        actions.add(new AliasAction.Add(aliasParameters(targetIndex, INDEX_ALIAS, true)));
        if (!targetIndexOperations.alias(actions)) {
            throw new BusinessException(ErrorCode.INDEX_SYNC_FAILED);
        }
    }

    private Set<String> aliasIndices(IndexOperations aliasIndexOperations) {
        return aliasIndexOperations.getAliasesForIndex(INDEX_ALIAS).entrySet().stream()
            .filter(entry -> entry.getValue().stream().anyMatch(this::isLockerSuggestAlias))
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toSet());
    }

    private boolean hasWriteAlias(IndexOperations aliasIndexOperations) {
        return aliasIndexOperations.getAliasesForIndex(INDEX_ALIAS).values().stream()
            .flatMap(Set::stream)
            .anyMatch(alias -> isLockerSuggestAlias(alias) && Boolean.TRUE.equals(alias.isWriteIndex()));
    }

    private boolean isLockerSuggestAlias(AliasData alias) {
        return INDEX_ALIAS.equals(alias.getAlias());
    }

    private AliasActionParameters aliasParameters(String index, String alias) {
        return aliasParameters(index, alias, null);
    }

    private AliasActionParameters aliasParameters(String index, String alias, Boolean writeIndex) {
        AliasActionParameters.Builder builder = AliasActionParameters.builder().withIndices(index);
        if (alias != null) {
            builder.withAliases(alias);
        }
        if (writeIndex != null) {
            builder.withIsWriteIndex(writeIndex);
        }
        return builder.build();
    }

    private String newVersionedIndexName() {
        return VERSIONED_INDEX_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    private void deleteIndexQuietly(String indexName) {
        try {
            IndexOperations indexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
            if (indexOperations.exists()) {
                indexOperations.delete();
            }
        } catch (Exception exception) {
            log.warn("검색 인덱스 정리 실패: {} [사유: {}]", indexName, exception.getMessage());
        }
    }

    private IndexSyncDataHolder loadSyncData(List<LockerSuggestIndexQueryProjection> projections) {
        List<Long> lockerIds = projections.stream()
            .map(LockerSuggestIndexQueryProjection::getLockerId)
            .toList();
        List<Long> placeIds = projections.stream()
            .map(LockerSuggestIndexQueryProjection::getPlaceId)
            .distinct()
            .toList();

        Map<Long, List<LockerTranslationEntity>> lockerTranslations =
            lockerTranslationRepository.findByLockerIdIn(lockerIds).stream()
                .collect(Collectors.groupingBy(lt -> lt.getLocker().getId()));

        Map<Long, List<LockerAliasEntity>> lockerAliases =
            lockerAliasRepository.findByLockerIdIn(lockerIds).stream()
                .collect(Collectors.groupingBy(la -> la.getLocker().getId()));

        Map<Long, List<PlaceTranslationEntity>> placeTranslations =
            placeTranslationRepository.findByPlaceIdIn(placeIds).stream()
                .collect(Collectors.groupingBy(pt -> pt.getPlace().getId()));

        Map<Long, List<PlaceAliasEntity>> placeAliases =
            placeAliasRepository.findByPlaceIdIn(placeIds).stream()
                .collect(Collectors.groupingBy(pa -> pa.getPlace().getId()));

        return new IndexSyncDataHolder(lockerTranslations, lockerAliases, placeTranslations, placeAliases);
    }

    private List<LockerSuggestDocument> toDocuments(List<LockerSuggestIndexQueryProjection> projections) {
        if (projections.isEmpty()) {
            return List.of();
        }

        IndexSyncDataHolder holder = loadSyncData(projections);
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

    private Map<Long, List<LockerSuggestIndexQueryProjection>> groupByPlace(
        List<LockerSuggestIndexQueryProjection> projections
    ) {
        Map<Long, List<LockerSuggestIndexQueryProjection>> map = new HashMap<>();
        for (LockerSuggestIndexQueryProjection p : projections) {
            map.computeIfAbsent(p.getPlaceId(), k -> new ArrayList<>()).add(p);
        }
        return map;
    }

    private Map<Long, GeoPoint> calculateCentroids(Map<Long, List<LockerSuggestIndexQueryProjection>> groups) {
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

    private List<String> buildPlaceSearchNames(
        LockerSuggestIndexQueryProjection p,
        List<PlaceTranslationEntity> pTrans,
        List<PlaceAliasEntity> pAliases
    ) {
        Set<String> placeSearchNameSet = new LinkedHashSet<>();
        if (p.getPlaceName() != null) {
            placeSearchNameSet.add(p.getPlaceName());
        }
        for (PlaceTranslationEntity pt : pTrans) {
            if (pt.getName() != null) {
                placeSearchNameSet.add(pt.getName());
            }
        }
        for (PlaceAliasEntity pa : pAliases) {
            if (pa.getAlias() != null) {
                placeSearchNameSet.add(pa.getAlias());
            }
            if (pa.getNormalizedAlias() != null) {
                placeSearchNameSet.add(pa.getNormalizedAlias());
            }
        }
        return normalizeAndFilter(placeSearchNameSet);
    }

    private List<String> buildLockerSearchNames(
        LockerSuggestIndexQueryProjection p,
        List<LockerTranslationEntity> lTrans,
        List<LockerAliasEntity> lAliases
    ) {
        Set<String> lockerSearchNameSet = new LinkedHashSet<>();
        if (p.getLockerName() != null) {
            lockerSearchNameSet.add(p.getLockerName());
        }
        for (LockerTranslationEntity lt : lTrans) {
            if (lt.getName() != null) {
                lockerSearchNameSet.add(lt.getName());
            }
        }
        for (LockerAliasEntity la : lAliases) {
            if (la.getAlias() != null) {
                lockerSearchNameSet.add(la.getAlias());
            }
            if (la.getNormalizedAlias() != null) {
                lockerSearchNameSet.add(la.getNormalizedAlias());
            }
        }
        return normalizeAndFilter(lockerSearchNameSet);
    }

    private List<String> buildSearchAddresses(
        LockerSuggestIndexQueryProjection p,
        List<PlaceTranslationEntity> pTrans,
        List<LockerTranslationEntity> lTrans
    ) {
        Set<String> searchAddressSet = new LinkedHashSet<>();
        if (p.getRoadAddress() != null) {
            searchAddressSet.add(p.getRoadAddress());
        }
        if (p.getPlaceRoadAddress() != null) {
            searchAddressSet.add(p.getPlaceRoadAddress());
        }
        for (PlaceTranslationEntity pt : pTrans) {
            if (pt.getRoadAddress() != null) {
                searchAddressSet.add(pt.getRoadAddress());
            }
        }
        for (LockerTranslationEntity lt : lTrans) {
            if (lt.getRoadAddress() != null) {
                searchAddressSet.add(lt.getRoadAddress());
            }
        }
        return normalizeAndFilter(searchAddressSet);
    }

    private Map<String, String> buildLocalizedLockerNames(List<LockerTranslationEntity> lTrans) {
        return lTrans.stream()
            .filter(lt -> lt.getName() != null && lt.getLanguage() != null)
            .collect(Collectors.toMap(
                lt -> lt.getLanguage().name().toLowerCase(),
                LockerTranslationEntity::getName,
                (v1, v2) -> v1
            ));
    }

    private Map<String, String> buildLocalizedRoadAddresses(List<LockerTranslationEntity> lTrans) {
        return lTrans.stream()
            .filter(lt -> lt.getRoadAddress() != null && lt.getLanguage() != null)
            .collect(Collectors.toMap(
                lt -> lt.getLanguage().name().toLowerCase(),
                LockerTranslationEntity::getRoadAddress,
                (v1, v2) -> v1
            ));
    }

    private Map<String, String> buildLocalizedPlaceNames(List<PlaceTranslationEntity> pTrans) {
        return pTrans.stream()
            .filter(pt -> pt.getName() != null && pt.getLanguage() != null)
            .collect(Collectors.toMap(
                pt -> pt.getLanguage().name().toLowerCase(),
                PlaceTranslationEntity::getName,
                (v1, v2) -> v1
            ));
    }

    private LockerSuggestDocument buildDocument(DocumentBuildContext ctx) {
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

    private LockerSuggestDocument fillPlaceAndTranslations(
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

    private List<String> normalizeAndFilter(Collection<String> values) {
        return values.stream()
            .map(SearchTextNormalizer::normalize)
            .filter(value -> value != null && !value.isBlank())
            .distinct()
            .toList();
    }

    private List<String> decompose(List<String> values) {
        return values.stream().map(HangulUtils::decompose).toList();
    }

    private List<String> parseLockerSizes(String lockerSizes) {
        if (lockerSizes == null || lockerSizes.isBlank()) {
            return List.of();
        }
        return Arrays.stream(lockerSizes.split(","))
            .map(LockerSizeType::from)
            .filter(java.util.Objects::nonNull)
            .map(Enum::name)
            .distinct()
            .toList();
    }

    private List<Long> distinctIds(Collection<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(ids.stream().filter(java.util.Objects::nonNull).toList()));
    }
}
