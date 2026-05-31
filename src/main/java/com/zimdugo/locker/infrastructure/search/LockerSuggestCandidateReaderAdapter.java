package com.zimdugo.locker.infrastructure.search;

import co.elastic.clients.elasticsearch._types.SortOrder;
import com.zimdugo.common.util.HangulUtils;
import com.zimdugo.locker.domain.LockerSuggestCandidate;
import com.zimdugo.locker.domain.LockerSuggestCandidateReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockerSuggestCandidateReaderAdapter implements LockerSuggestCandidateReader {

    private static final int FETCH_MULTIPLIER = 4;
    private static final int MIN_FETCH_SIZE = 20;
    private static final int MAX_FETCH_SIZE = 200;

    private static final float PLACE_AUTO_BOOST = 5.0F;
    private static final float LOCKER_AUTO_BOOST = 4.0F;
    private static final float PLACE_DECOMPOSED_BOOST = 3.5F;
    private static final float LOCKER_DECOMPOSED_BOOST = 3.0F;

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<LockerSuggestCandidate> search(double latitude, double longitude, String keyword, int limit) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isBlank() || limit <= 0) {
            return List.of();
        }

        int fetchSize = Math.min(Math.max(limit * FETCH_MULTIPLIER, MIN_FETCH_SIZE), MAX_FETCH_SIZE);
        NativeQuery query = buildSearchQuery(normalizedKeyword, latitude, longitude, fetchSize);

        SearchHits<LockerSuggestDocument> hits = elasticsearchOperations.search(query, LockerSuggestDocument.class);
        return convertToCandidates(hits);
    }

    private NativeQuery buildSearchQuery(String keyword, double lat, double lon, int fetchSize) {
        String decomposed = HangulUtils.decompose(keyword);
        return NativeQuery.builder()
            .withQuery(q -> q.bool(b -> b
                .must(m -> m.bool(sb -> sb
                    .should(s -> s.matchPhrasePrefix(ma -> ma
                        .field("placeName.autocomplete").query(keyword).boost(PLACE_AUTO_BOOST)))
                    .should(s -> s.matchPhrasePrefix(ma -> ma
                        .field("lockerName.autocomplete").query(keyword).boost(LOCKER_AUTO_BOOST)))
                    .should(s -> s.matchPhrasePrefix(ma -> ma
                        .field("placeNameDecomposed.autocomplete")
                        .query(decomposed).boost(PLACE_DECOMPOSED_BOOST)))
                    .should(s -> s.matchPhrasePrefix(ma -> ma
                        .field("lockerNameDecomposed.autocomplete")
                        .query(decomposed).boost(LOCKER_DECOMPOSED_BOOST)))
                ))
            ))
            .withSort(s -> s.geoDistance(g -> g
                .field("placeLocation")
                .location(l -> l.latlon(ll -> ll.lat(lat).lon(lon)))
                .order(SortOrder.Asc)
            ))
            .withPageable(PageRequest.of(0, fetchSize))
            .build();
    }

    private List<LockerSuggestCandidate> convertToCandidates(SearchHits<LockerSuggestDocument> hits) {
        List<LockerSuggestCandidate> candidates = new ArrayList<>(hits.getSearchHits().size());
        for (SearchHit<LockerSuggestDocument> hit : hits.getSearchHits()) {
            candidates.add(toCandidate(hit));
        }

        candidates.sort(Comparator.comparingDouble(LockerSuggestCandidate::score).reversed()
            .thenComparingLong(LockerSuggestCandidate::distanceMeters));
        return candidates;
    }

    private LockerSuggestCandidate toCandidate(SearchHit<LockerSuggestDocument> hit) {
        LockerSuggestDocument doc = hit.getContent();
        double distanceMeters = 0;
        if (!hit.getSortValues().isEmpty()) {
            distanceMeters = Double.parseDouble(hit.getSortValues().get(0).toString());
        }

        return new LockerSuggestCandidate(
            doc.getLockerId(), doc.getLockerName(), doc.getRoadAddress(),
            doc.getLockerType(), doc.getUpdatedAt(), doc.getPlaceId(),
            doc.getPlaceName(), doc.getLockerCount(),
            (long) distanceMeters, hit.getScore()
        );
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }
}
