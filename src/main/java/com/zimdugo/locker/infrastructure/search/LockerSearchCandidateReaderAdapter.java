package com.zimdugo.locker.infrastructure.search;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.zimdugo.common.i18n.CurrentRequestLanguage;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.common.i18n.SearchTextNormalizer;
import com.zimdugo.common.util.HangulUtils;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.LockerSearchCandidateResult;
import com.zimdugo.locker.domain.LockerSearchCandidateReader;
import com.zimdugo.locker.domain.LockerSearchFilter;
import com.zimdugo.locker.domain.LockerSuggestCandidate;
import com.zimdugo.locker.domain.LockerType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
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

    private static final int MAX_FETCH_SIZE = 200;

    private static final float PLACE_AUTO_BOOST = 5.0F;
    private static final float LOCKER_AUTO_BOOST = 4.0F;
    private static final float PLACE_DECOMPOSED_BOOST = 3.5F;
    private static final float LOCKER_DECOMPOSED_BOOST = 3.0F;
    private static final float ADDRESS_AUTO_BOOST = 2.0F;
    private static final float ADDRESS_DECOMPOSED_BOOST = 1.5F;
    private static final float PLACE_LANG_BOOST = 4.0F;
    private static final float LOCKER_LANG_BOOST = 3.0F;

    private static final Pattern HANGUL_PATTERN = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]");
    private static final Pattern ALPHA_PATTERN = Pattern.compile("[a-zA-Z]");
    private static final Pattern JAPANESE_PATTERN = Pattern.compile("[\\u3040-\\u309F\\u30A0-\\u30FF]");
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4E00-\\u9FBF]");

    private final ElasticsearchOperations elasticsearchOperations;
    private final CurrentRequestLanguage currentRequestLanguage;

    @Override
    public LockerSearchCandidateResult search(
        double latitude,
        double longitude,
        String keyword,
        LockerSearchFilter filter
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isBlank()) {
            return LockerSearchCandidateResult.empty();
        }

        SupportedLanguage requestedLanguage = currentRequestLanguage.resolve();

        int fetchSize = MAX_FETCH_SIZE;
        NativeQuery nameQuery = buildSearchQuery(
            buildFilteredQuery(buildNameQuery(normalizedKeyword), filter),
            latitude,
            longitude,
            fetchSize
        );
        SearchHits<LockerSuggestDocument> nameHits =
            elasticsearchOperations.search(nameQuery, LockerSuggestDocument.class);
        if (!nameHits.getSearchHits().isEmpty()) {
            return LockerSearchCandidateResult.name(convertToCandidates(nameHits, requestedLanguage));
        }

        NativeQuery addressQuery = buildSearchQuery(
            buildFilteredQuery(buildAddressQuery(normalizedKeyword), filter),
            latitude,
            longitude,
            fetchSize
        );
        SearchHits<LockerSuggestDocument> addressHits =
            elasticsearchOperations.search(addressQuery, LockerSuggestDocument.class);
        return LockerSearchCandidateResult.address(convertToCandidates(addressHits, requestedLanguage));
    }

    private NativeQuery buildSearchQuery(Query query, double lat, double lon, int fetchSize) {
        return NativeQuery.builder()
            .withQuery(query)
            .withSort(s -> s.score(score -> score.order(SortOrder.Desc)))
            .withSort(s -> s.geoDistance(g -> g
                .field("placeLocation")
                .location(l -> l.latlon(ll -> ll.lat(lat).lon(lon)))
                .order(SortOrder.Asc)
            ))
            .withPageable(PageRequest.of(0, fetchSize))
            .build();
    }

    private Query buildNameQuery(String keyword) {
        SearchTargets targets = detectSearchTargets(keyword);
        String decomposed = targets.searchKo() ? HangulUtils.decompose(keyword) : keyword;

        return Query.of(q -> q.bool(b -> b.must(m -> m.bool(sb -> sb
            .should(s -> s.bool(type -> buildPlaceNameQuery(type, keyword, decomposed, targets)))
            .should(s -> s.bool(type -> buildLockerNameQuery(type, keyword, decomposed, targets)))
        ))));
    }

    private SearchTargets detectSearchTargets(String keyword) {
        boolean hasHangul = HANGUL_PATTERN.matcher(keyword).find();
        boolean hasAlpha = ALPHA_PATTERN.matcher(keyword).find();
        boolean hasJapanese = JAPANESE_PATTERN.matcher(keyword).find();
        boolean hasChinese = CHINESE_PATTERN.matcher(keyword).find();

        boolean detectAny = hasHangul || hasAlpha || hasJapanese || hasChinese;

        return new SearchTargets(
            !detectAny || hasHangul,
            !detectAny || hasAlpha,
            !detectAny || hasJapanese || hasChinese,
            !detectAny || hasChinese
        );
    }

    private co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder buildPlaceNameQuery(
        co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder type,
        String keyword,
        String decomposed,
        SearchTargets targets
    ) {
        type.queryName(LockerSuggestCandidate.PLACE_NAME_QUERY);
        type.should(name -> name.matchPhrasePrefix(ma -> ma
            .field("placeSearchNames.autocomplete").query(keyword).boost(PLACE_AUTO_BOOST)));
        type.should(name -> name.matchPhrasePrefix(ma -> ma
            .field("placeSearchNamesDecomposed.autocomplete").query(decomposed).boost(PLACE_DECOMPOSED_BOOST)));

        if (targets.searchKo()) {
            type.should(name -> name.match(ma -> ma
                .field("placeSearchNames.ko").query(keyword).operator(Operator.And).boost(PLACE_AUTO_BOOST)));
        }
        if (targets.searchEn()) {
            type.should(name -> name.match(ma -> ma
                .field("placeSearchNames.en").query(keyword).operator(Operator.And).boost(PLACE_LANG_BOOST)));
        }
        if (targets.searchJa()) {
            type.should(name -> name.match(ma -> ma
                .field("placeSearchNames.ja").query(keyword).operator(Operator.And).boost(PLACE_LANG_BOOST)));
        }
        if (targets.searchZh()) {
            type.should(name -> name.match(ma -> ma
                .field("placeSearchNames.zh").query(keyword).operator(Operator.And).boost(PLACE_LANG_BOOST)));
        }
        return type;
    }

    private co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder buildLockerNameQuery(
        co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder type,
        String keyword,
        String decomposed,
        SearchTargets targets
    ) {
        type.queryName(LockerSuggestCandidate.LOCKER_NAME_QUERY);
        type.should(name -> name.matchPhrasePrefix(ma -> ma
            .field("lockerSearchNames.autocomplete").query(keyword).boost(LOCKER_AUTO_BOOST)));
        type.should(name -> name.matchPhrasePrefix(ma -> ma
            .field("lockerSearchNamesDecomposed.autocomplete").query(decomposed).boost(LOCKER_DECOMPOSED_BOOST)));

        if (targets.searchKo()) {
            type.should(name -> name.match(ma -> ma
                .field("lockerSearchNames.ko").query(keyword).operator(Operator.And).boost(LOCKER_AUTO_BOOST)));
        }
        if (targets.searchEn()) {
            type.should(name -> name.match(ma -> ma
                .field("lockerSearchNames.en").query(keyword).operator(Operator.And).boost(LOCKER_LANG_BOOST)));
        }
        if (targets.searchJa()) {
            type.should(name -> name.match(ma -> ma
                .field("lockerSearchNames.ja").query(keyword).operator(Operator.And).boost(LOCKER_LANG_BOOST)));
        }
        if (targets.searchZh()) {
            type.should(name -> name.match(ma -> ma
                .field("lockerSearchNames.zh").query(keyword).operator(Operator.And).boost(LOCKER_LANG_BOOST)));
        }
        return type;
    }

    private record SearchTargets(
        boolean searchKo,
        boolean searchEn,
        boolean searchJa,
        boolean searchZh
    ) {}

    private Query buildAddressQuery(String keyword) {
        boolean hasHangul = HANGUL_PATTERN.matcher(keyword).find();
        String decomposed = hasHangul ? HangulUtils.decompose(keyword) : keyword;
        return Query.of(q -> q.bool(b -> b.must(m -> m.bool(sb -> sb
            .should(s -> s.matchPhrasePrefix(ma -> ma
                .field("searchAddresses.autocomplete").query(keyword).boost(ADDRESS_AUTO_BOOST)))
            .should(s -> s.matchPhrasePrefix(ma -> ma
                .field("searchAddressesDecomposed.autocomplete").query(decomposed).boost(ADDRESS_DECOMPOSED_BOOST)))
        ))));
    }

    private Query buildFilteredQuery(Query query, LockerSearchFilter filter) {
        if (filter.isEmpty()) {
            return query;
        }
 
        return Query.of(q -> q.bool(b -> {
            b.must(query);
            addTermsFilter(b, "lockerSize", filter.sizeTypes());
            addTermsFilter(b, "indoorOutdoorType", filter.indoorOutdoorTypes());
            addTermsFilter(b, "lockerType", filter.lockerTypes());
            return b;
        }));
    }
 
    private <T extends Enum<T>> void addTermsFilter(
        co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder builder,
        String fieldName,
        Set<T> filterValues
    ) {
        if (filterValues.isEmpty()) {
            return;
        }
        List<FieldValue> values = filterValues.stream()
            .map(val -> FieldValue.of(val.name()))
            .toList();
        builder.filter(f -> f.terms(t -> t
            .field(fieldName)
            .terms(v -> v.value(values))
        ));
    }

    private List<LockerSuggestCandidate> convertToCandidates(
        SearchHits<LockerSuggestDocument> hits,
        SupportedLanguage requestedLanguage
    ) {
        List<SearchHit<LockerSuggestDocument>> searchHits = hits.getSearchHits();
        List<LockerSuggestCandidate> candidates = new ArrayList<>(searchHits.size());
        for (SearchHit<LockerSuggestDocument> hit : searchHits) {
            candidates.add(toCandidate(hit, requestedLanguage));
        }
        return candidates;
    }

    private LockerSuggestCandidate toCandidate(
        SearchHit<LockerSuggestDocument> hit,
        SupportedLanguage requestedLanguage
    ) {
        LockerSuggestDocument doc = hit.getContent();
        GeoPoint lockerPoint = requireIndexLocation(doc.getLocation());
        GeoPoint placePoint = requireIndexLocation(doc.getPlaceLocation());

        double distanceMeters = 0;
        if (hit.getSortValues().size() > 1) {
            distanceMeters = Double.parseDouble(hit.getSortValues().get(1).toString());
        }

        String langKey = requestedLanguage.name().toLowerCase();
        String lockerName = doc.getLocalizedLockerNames() != null && doc.getLocalizedLockerNames().containsKey(langKey)
            ? doc.getLocalizedLockerNames().get(langKey)
            : doc.getLockerName();

        String roadAddress = doc.getLocalizedRoadAddresses() != null && doc.getLocalizedRoadAddresses().containsKey(langKey)
            ? doc.getLocalizedRoadAddresses().get(langKey)
            : doc.getRoadAddress();

        String placeName = doc.getLocalizedPlaceNames() != null && doc.getLocalizedPlaceNames().containsKey(langKey)
            ? doc.getLocalizedPlaceNames().get(langKey)
            : doc.getPlaceName();

        return new LockerSuggestCandidate(
            doc.getLockerId(),
            lockerName,
            roadAddress,
            LockerType.valueOf(doc.getLockerType()), doc.getMinPrice(), doc.getUpdatedAt(), doc.getPlaceId(),
            placeName, Set.copyOf(hit.getMatchedQueries().keySet()),
            doc.getLockerCount(),
            (long) distanceMeters,
            lockerPoint.getLat(),
            lockerPoint.getLon(),
            placePoint.getLat(),
            placePoint.getLon(),
            hit.getScore()
        );
    }

    private GeoPoint requireIndexLocation(GeoPoint location) {
        if (location == null) {
            throw new BusinessException(ErrorCode.SEARCH_INDEX_DATA_INVALID);
        }
        return location;
    }

    private String normalizeKeyword(String keyword) {
        return SearchTextNormalizer.normalize(keyword);
    }
}
