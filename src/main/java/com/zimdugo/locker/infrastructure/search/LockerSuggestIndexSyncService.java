package com.zimdugo.locker.infrastructure.search;

import com.zimdugo.common.util.HangulUtils;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.infrastructure.LockerRepository;
import com.zimdugo.locker.infrastructure.LockerSuggestIndexQueryProjection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockerSuggestIndexSyncService {

    private final LockerRepository lockerRepository;
    private final LockerSuggestSearchRepository lockerSuggestSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void syncAtStartup() {
        log.info("보관함 검색 인덱스 동기화 시작...");
        try {
            IndexOperations indexOperations = elasticsearchOperations.indexOps(LockerSuggestDocument.class);
            if (!indexOperations.exists()) {
                log.info("검색 인덱스가 존재하지 않아 새로 생성합니다.");
                indexOperations.createWithMapping();
            }

            List<LockerSuggestDocument> documents = toDocuments(lockerRepository.findAllForSuggestIndex());
            if (documents.isEmpty()) {
                log.warn("동기화할 보관함 데이터가 없습니다.");
                return;
            }

            lockerSuggestSearchRepository.saveAll(documents);

            log.info("보관함 검색 인덱스 동기화 완료: {}건", documents.size());
        } catch (DataAccessException e) {
            log.error("검색 인덱스 동기화 실패: DB 조회 중 오류 발생 [사유: {}]", e.getMessage());
            throw new BusinessException(ErrorCode.INDEX_SYNC_FAILED, e);
        } catch (Exception e) {
            log.error("검색 인덱스 동기화 실패: 알 수 없는 오류 [종류: {}, 사유: {}]",
                e.getClass().getSimpleName(), e.getMessage());
            throw new BusinessException(ErrorCode.INDEX_SYNC_FAILED, e);
        }
    }

    private List<LockerSuggestDocument> toDocuments(List<LockerSuggestIndexQueryProjection> projections) {
        Map<Long, List<LockerSuggestIndexQueryProjection>> projectionsByPlace = groupByPlace(projections);
        Map<Long, GeoPoint> centerByPlace = calculateCentroids(projectionsByPlace);

        List<LockerSuggestDocument> documents = new ArrayList<>(projections.size());
        for (Map.Entry<Long, List<LockerSuggestIndexQueryProjection>> entry : projectionsByPlace.entrySet()) {
            Long placeId = entry.getKey();
            List<LockerSuggestIndexQueryProjection> placeProjections = entry.getValue();
            GeoPoint center = centerByPlace.get(placeId);
            int lockerCount = placeProjections.size();

            for (LockerSuggestIndexQueryProjection projection : placeProjections) {
                documents.add(buildDocument(projection, center, lockerCount));
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

    private LockerSuggestDocument buildDocument(
        LockerSuggestIndexQueryProjection p,
        GeoPoint placeCenter,
        int lockerCount
    ) {
        return LockerSuggestDocument.builder()
            .id(String.valueOf(p.getLockerId()))
            .lockerId(p.getLockerId())
            .lockerName(p.getLockerName())
            .lockerNameDecomposed(HangulUtils.decompose(p.getLockerName()))
            .roadAddress(p.getRoadAddress())
            .roadAddressDecomposed(HangulUtils.decompose(p.getRoadAddress()))
            .lockerType(p.getLockerType())
            .updatedAt(p.getUpdatedAt())
            .placeId(p.getPlaceId())
            .placeName(p.getPlaceName())
            .placeNameDecomposed(HangulUtils.decompose(p.getPlaceName()))
            .location(new GeoPoint(p.getLockerLatitude(), p.getLockerLongitude()))
            .placeLocation(placeCenter)
            .lockerCount(lockerCount)
            .build();
    }
}
