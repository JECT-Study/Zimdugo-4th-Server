package com.zimdugo.locker.infrastructure.search;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.infrastructure.persistence.LockerRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerAliasRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceAliasRepository;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationRepository;
import com.zimdugo.locker.infrastructure.persistence.PlaceTranslationRepository;
import com.zimdugo.locker.infrastructure.projection.LockerSuggestIndexQueryProjection;
import com.zimdugo.locker.infrastructure.persistence.LockerAliasEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceAliasEntity;
import com.zimdugo.locker.infrastructure.persistence.LockerTranslationEntity;
import com.zimdugo.locker.infrastructure.persistence.PlaceTranslationEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexInformation;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.index.AliasData;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockerSuggestIndexSyncService {

    private static final String INDEX_ALIAS = "locker_suggest";
    private static final String VERSIONED_INDEX_PREFIX = INDEX_ALIAS + "_v_";
    private static final int MAX_SAVE_ATTEMPTS = 3;
    private static final int BACKOFF_MS = 2000;
    private static final int MILLIS_IN_SECOND = 1000;

    private final LockerRepository lockerRepository;
    private final LockerSuggestSearchRepository lockerSuggestSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final LockerAliasRepository lockerAliasRepository;
    private final PlaceAliasRepository placeAliasRepository;
    private final LockerTranslationRepository lockerTranslationRepository;
    private final PlaceTranslationRepository placeTranslationRepository;
    private final LockerSuggestIndexAvailability indexAvailability;

    @EventListener(ApplicationReadyEvent.class)
    @Async("lockerSuggestIndexSyncExecutor")
    @Transactional(readOnly = true)
    public void syncAtStartup() {
        String targetIndex = newVersionedIndexName();
        log.info("보관함 검색 인덱스 동기화 시작...");
        try {
            if (hasServingIndex()) {
                indexAvailability.markAvailable();
            }
            List<LockerSuggestDocument> documents = toDocuments(lockerRepository.findAllForSuggestIndex());
            rebuildAndSwitchAlias(targetIndex, documents);
            indexAvailability.markAvailable();
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

    private boolean hasServingIndex() {
        IndexOperations aliasIndexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(INDEX_ALIAS));
        return aliasIndexOperations.exists() && hasWriteAlias(aliasIndexOperations);
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
            saveWithRetry(documents, targetIndex);
        }
        targetIndexOperations.refresh();

        IndexOperations aliasIndexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(INDEX_ALIAS));
        Set<String> previousIndices = aliasIndexOperations.exists() ? aliasIndices(aliasIndexOperations) : Set.of();

        switchAlias(targetIndex, targetIndexOperations, previousIndices);
        log.info("검색 인덱스 alias 전환 완료: {} -> {}", INDEX_ALIAS, targetIndex);

        cleanUpOldIndices(targetIndex, previousIndices);
    }

    private void saveWithRetry(List<LockerSuggestDocument> documents, String targetIndex) {
        for (int attempt = 1; attempt <= MAX_SAVE_ATTEMPTS; attempt++) {
            try {
                elasticsearchOperations.save(documents, IndexCoordinates.of(targetIndex));
                return;
            } catch (Exception e) {
                if (attempt == MAX_SAVE_ATTEMPTS) {
                    throw e;
                }
                log.warn("검색 인덱스 색인 실패 (시도 {}/{}). {}초 후 재시도합니다. [사유: {}]",
                    attempt, MAX_SAVE_ATTEMPTS, BACKOFF_MS / MILLIS_IN_SECOND, e.getMessage());
                try {
                    Thread.sleep(BACKOFF_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(ErrorCode.INDEX_SYNC_FAILED, ie);
                }
            }
        }
    }

    private void cleanUpOldIndices(String targetIndex, Set<String> previousIndices) {
        try {
            IndexOperations allIndicesOps = elasticsearchOperations.indexOps(
                IndexCoordinates.of(VERSIONED_INDEX_PREFIX + "*")
            );
            List<IndexInformation> indexInfos = allIndicesOps.getInformation();
            if (indexInfos == null) {
                return;
            }
            for (IndexInformation info : indexInfos) {
                String indexName = info.getName();
                if (indexName != null && !indexName.equals(targetIndex) && !previousIndices.contains(indexName)) {
                    log.info("오래된 검색 인덱스 삭제 중: {}", indexName);
                    deleteIndexQuietly(indexName);
                }
            }
        } catch (Exception e) {
            log.warn("오래된 검색 인덱스 정리 중 오류 발생 [사유: {}]", e.getMessage());
        }
    }

    private void switchAlias(String targetIndex, IndexOperations targetIndexOperations, Set<String> previousIndices) {
        IndexOperations aliasIndexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(INDEX_ALIAS));
        boolean logicalIndexExists = aliasIndexOperations.exists();
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
            .collect(Collectors.toSet());
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
        return LockerSuggestDocumentMapper.toDocuments(projections, holder);
    }

    private List<Long> distinctIds(Collection<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(ids.stream().filter(Objects::nonNull).toList()));
    }
}
