package com.zimdugo.locker.infrastructure.search;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.zimdugo.common.util.HangulUtils;
import com.zimdugo.locker.domain.LockerSearchCandidateResult;
import com.zimdugo.locker.domain.LockerSearchCandidateReader;
import com.zimdugo.locker.domain.LockerSearchFilter;
import com.zimdugo.locker.domain.LockerSuggestCandidate;
import com.zimdugo.locker.domain.LockerType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockerSearchCandidateReaderAdapter implements LockerSearchCandidateReader {

    private static final int FETCH_MULTIPLIER = 4;
    private static final int MIN_FETCH_SIZE = 20;
    private static final int MAX_FETCH_SIZE = 200;

    private static final float PLACE_AUTO_BOOST = 5.0F;
    private static final float LOCKER_AUTO_BOOST = 4.0F;
    private static final float PLACE_DECOMPOSED_BOOST = 3.5F;
    private static final float LOCKER_DECOMPOSED_BOOST = 3.0F;
    private static final float ADDRESS_AUTO_BOOST = 2.0F;
    private static final float ADDRESS_DECOMPOSED_BOOST = 1.5F;

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public LockerSearchCandidateResult search(
        double latitude,
        double longitude,
        String keyword,
        int limit,
        LockerSearchFilter filter
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isBlank() || limit <= 0) {
            return LockerSearchCandidateResult.empty();
        }

        int fetchSize = Math.min(Math.max(limit * FETCH_MULTIPLIER, MIN_FETCH_SIZE), MAX_FETCH_SIZE);
        NativeQuery nameQuery = buildSearchQuery(
            buildFilteredQuery(buildNameQuery(normalizedKeyword), filter),
            latitude,
            longitude,
            fetchSize
        );
        SearchHits<LockerSuggestDocument> nameHits =
            elasticsearchOperations.search(nameQuery, LockerSuggestDocument.class);
        if (!nameHits.getSearchHits().isEmpty()) {
            return LockerSearchCandidateResult.name(convertToCandidates(nameHits));
        }

        NativeQuery addressQuery = buildSearchQuery(
            buildFilteredQuery(buildAddressQuery(normalizedKeyword), filter),
            latitude,
            longitude,
            fetchSize
        );
        SearchHits<LockerSuggestDocument> addressHits =
            elasticsearchOperations.search(addressQuery, LockerSuggestDocument.class);
        return LockerSearchCandidateResult.address(convertToCandidates(addressHits));
    }

    private NativeQuery buildSearchQuery(Query query, double lat, double lon, int fetchSize) {
        return NativeQuery.builder()
            .withQuery(query)
            .withSort(s -> s.geoDistance(g -> g
                .field("placeLocation")
                .location(l -> l.latlon(ll -> ll.lat(lat).lon(lon)))
                .order(SortOrder.Asc)
            ))
            .withPageable(PageRequest.of(0, fetchSize))
            .build();
    }

    private Query buildNameQuery(String keyword) {
        String decomposed = HangulUtils.decompose(keyword);
        return Query.of(q -> q.bool(b -> b.must(m -> m.bool(sb -> sb
            .should(s -> s.matchPhrasePrefix(ma -> ma
                .field("placeName.autocomplete").query(keyword).boost(PLACE_AUTO_BOOST)))
            .should(s -> s.matchPhrasePrefix(ma -> ma
                .field("lockerName.autocomplete").query(keyword).boost(LOCKER_AUTO_BOOST)))
            .should(s -> s.matchPhrasePrefix(ma -> ma
                .field("placeNameDecomposed.autocomplete").query(decomposed).boost(PLACE_DECOMPOSED_BOOST)))
            .should(s -> s.matchPhrasePrefix(ma -> ma
                .field("lockerNameDecomposed.autocomplete").query(decomposed).boost(LOCKER_DECOMPOSED_BOOST)))
        ))));
    }

    private Query buildAddressQuery(String keyword) {
        String decomposed = HangulUtils.decompose(keyword);
        return Query.of(q -> q.bool(b -> b.must(m -> m.bool(sb -> sb
            .should(s -> s.matchPhrasePrefix(ma -> ma
                .field("roadAddress.autocomplete").query(keyword).boost(ADDRESS_AUTO_BOOST)))
            .should(s -> s.matchPhrasePrefix(ma -> ma
                .field("roadAddressDecomposed.autocomplete").query(decomposed).boost(ADDRESS_DECOMPOSED_BOOST)))
        ))));
    }

    private Query buildFilteredQuery(Query query, LockerSearchFilter filter) {
        if (filter.isEmpty()) {
            return query;
        }

        return Query.of(q -> q.bool(b -> {
            b.must(query);
            if (!filter.sizeTypes().isEmpty()) {
                List<FieldValue> values = filter.sizeTypes().stream()
                    .map(sizeType -> FieldValue.of(sizeType.name()))
                    .toList();
                b.filter(f -> f.terms(t -> t
                    .field("lockerSize")
                    .terms(v -> v.value(values))
                ));
            }
            if (filter.indoorOutdoorType() != null) {
                b.filter(f -> f.term(t -> t
                    .field("indoorOutdoorType")
                    .value(filter.indoorOutdoorType().name())
                ));
            }
            if (filter.lockerType() != null) {
                b.filter(f -> f.term(t -> t
                    .field("lockerType")
                    .value(filter.lockerType().name())
                ));
            }
            return b;
        }));
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
        GeoPoint lockerPoint = Objects.requireNonNull(doc.getLocation(), "locker_suggest.location must not be null");
        GeoPoint placePoint = Objects.requireNonNull(
            doc.getPlaceLocation(),
            "locker_suggest.placeLocation must not be null"
        );

        double distanceMeters = 0;
        if (!hit.getSortValues().isEmpty()) {
            distanceMeters = Double.parseDouble(hit.getSortValues().get(0).toString());
        }

        return new LockerSuggestCandidate(
            doc.getLockerId(), doc.getLockerName(), doc.getRoadAddress(),
            LockerType.valueOf(doc.getLockerType()), doc.getUpdatedAt(), doc.getPlaceId(),
            doc.getPlaceName(), doc.getLockerCount(),
            (long) distanceMeters,
            lockerPoint.getLat(),
            lockerPoint.getLon(),
            placePoint.getLat(),
            placePoint.getLon(),
            hit.getScore()
        );
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }
}
