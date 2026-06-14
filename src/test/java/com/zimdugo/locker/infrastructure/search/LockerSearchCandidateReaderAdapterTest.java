package com.zimdugo.locker.infrastructure.search;

import co.elastic.clients.elasticsearch._types.SortOrder;
import com.zimdugo.common.i18n.CurrentRequestLanguage;
import com.zimdugo.common.i18n.SupportedLanguage;
import com.zimdugo.locker.domain.LockerSearchCandidateResult;
import com.zimdugo.locker.domain.IndoorOutdoorType;
import com.zimdugo.locker.domain.LockerSearchFilter;
import com.zimdugo.locker.domain.LockerSearchMatchType;
import com.zimdugo.locker.domain.LockerSizeType;
import com.zimdugo.locker.domain.LockerType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LockerSearchCandidateReaderAdapterTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private SearchHits<LockerSuggestDocument> searchHits;

    @Mock
    private CurrentRequestLanguage currentRequestLanguage;

    @InjectMocks
    private LockerSearchCandidateReaderAdapter lockerSearchCandidateReaderAdapter;

    @Test
    @DisplayName("이름 검색 결과가 없으면 도로명주소로 다시 검색한다")
    void searchesRoadAddressWhenNameSearchIsEmpty() {
        given(elasticsearchOperations.search(
            any(NativeQuery.class),
            eq(LockerSuggestDocument.class)
        )).willReturn(searchHits, searchHits);
        given(searchHits.getSearchHits()).willReturn(java.util.List.of());
        given(currentRequestLanguage.resolve()).willReturn(SupportedLanguage.KOREAN);

        LockerSearchCandidateResult result = lockerSearchCandidateReaderAdapter.search(
            37.55, 126.93, "중앙로", LockerSearchFilter.empty()
        );

        ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);
        verify(elasticsearchOperations, times(2)).search(captor.capture(), eq(LockerSuggestDocument.class));

        assertThat(result.matchType()).isEqualTo(LockerSearchMatchType.ADDRESS);
        List<NativeQuery> queries = captor.getAllValues();
        assertThat(queries.get(0).getQuery().toString()).contains("placeSearchNames.autocomplete");
        assertThat(queries.get(0).getQuery().toString()).doesNotContain("searchAddresses.autocomplete");
        assertThat(queries.get(1).getQuery().toString()).contains("searchAddresses.autocomplete");
        assertThat(queries.get(1).getQuery().toString()).contains("searchAddressesDecomposed.autocomplete");
        assertThat(queries).allSatisfy(this::assertRelevanceFirstSort);
    }

    @Test
    @DisplayName("이름 검색 결과가 있으면 도로명주소 검색을 실행하지 않는다")
    void doesNotSearchRoadAddressWhenNameSearchHasResult() {
        List<SearchHit<LockerSuggestDocument>> searchHitList = List.of(searchHit());
        given(elasticsearchOperations.search(
            any(NativeQuery.class),
            eq(LockerSuggestDocument.class)
        )).willReturn(searchHits);
        given(searchHits.getSearchHits()).willReturn(searchHitList);
        given(currentRequestLanguage.resolve()).willReturn(SupportedLanguage.KOREAN);

        LockerSearchCandidateResult result = lockerSearchCandidateReaderAdapter.search(
            37.55, 126.93, "신촌", LockerSearchFilter.empty()
        );

        ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);
        verify(elasticsearchOperations).search(captor.capture(), eq(LockerSuggestDocument.class));
        assertThat(result.matchType()).isEqualTo(LockerSearchMatchType.NAME);
        assertThat(result.candidates()).hasSize(1);
        assertThat(result.candidates().getFirst().distanceMeters()).isEqualTo(100L);
        assertThat(result.candidates().getFirst().matchedQueries())
            .containsExactly(com.zimdugo.locker.domain.LockerSuggestCandidate.PLACE_NAME_QUERY);
        assertRelevanceFirstSort(captor.getValue());
        String query = captor.getValue().getQuery().toString();
        assertThat(query)
            .contains(
                "placeSearchNames.autocomplete",
                "lockerSearchNames.autocomplete",
                "placeSearchNamesDecomposed.autocomplete",
                "lockerSearchNamesDecomposed.autocomplete"
            )
            .doesNotContain("searchAddresses.autocomplete");
        assertThat(query)
            .containsOnlyOnce(com.zimdugo.locker.domain.LockerSuggestCandidate.PLACE_NAME_QUERY)
            .containsOnlyOnce(com.zimdugo.locker.domain.LockerSuggestCandidate.LOCKER_NAME_QUERY);
    }

    @Test
    @DisplayName("복수 사이즈와 실내외, 보관함 유형 필터를 이름과 주소 검색에 함께 적용한다")
    void appliesKeywordFiltersToNameAndAddressQueries() {
        given(elasticsearchOperations.search(
            any(NativeQuery.class),
            eq(LockerSuggestDocument.class)
        )).willReturn(searchHits, searchHits);
        given(searchHits.getSearchHits()).willReturn(java.util.List.of());
        given(currentRequestLanguage.resolve()).willReturn(SupportedLanguage.KOREAN);
        LockerSearchFilter filter = new LockerSearchFilter(
            Set.of(LockerSizeType.SMALL, LockerSizeType.LARGE),
            Set.of(IndoorOutdoorType.INDOOR),
            Set.of(LockerType.SUBWAY_STATION)
        );

        lockerSearchCandidateReaderAdapter.search(37.55, 126.93, "중앙로", filter);

        ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);
        verify(elasticsearchOperations, times(2)).search(captor.capture(), eq(LockerSuggestDocument.class));
        for (NativeQuery query : captor.getAllValues()) {
            String queryText = query.getQuery().toString();
            assertThat(queryText).contains("lockerSize", "SMALL", "LARGE");
            assertThat(queryText).contains("indoorOutdoorType", "INDOOR");
            assertThat(queryText).contains("lockerType", "SUBWAY_STATION");
        }
    }

    @Test
    @DisplayName("검색어에 NFKC, 소문자화, 공백 제거 정규화를 적용한다")
    void normalizesKeywordBeforeSearching() {
        given(elasticsearchOperations.search(
            any(NativeQuery.class),
            eq(LockerSuggestDocument.class)
        )).willReturn(searchHits, searchHits);
        given(searchHits.getSearchHits()).willReturn(List.of());
        given(currentRequestLanguage.resolve()).willReturn(SupportedLanguage.KOREAN);

        lockerSearchCandidateReaderAdapter.search(37.55, 126.93, " Ｓｅｏｕｌ\tStation ", LockerSearchFilter.empty());

        ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);
        verify(elasticsearchOperations, times(2)).search(captor.capture(), eq(LockerSuggestDocument.class));
        assertThat(captor.getAllValues())
            .allSatisfy(query -> assertThat(query.getQuery().toString()).contains("seoulstation"));
    }

    @Test
    @DisplayName("정규화 후 빈 검색어이면 Elasticsearch를 호출하지 않는다")
    void doesNotSearchWhenNormalizedKeywordIsBlank() {
        LockerSearchCandidateResult result = lockerSearchCandidateReaderAdapter.search(
            37.55,
            126.93,
            " \t\u00a0",
            LockerSearchFilter.empty()
        );

        assertThat(result.candidates()).isEmpty();
        verifyNoInteractions(elasticsearchOperations, currentRequestLanguage);
    }

    private void assertRelevanceFirstSort(NativeQuery query) {
        assertThat(query.getPageable().getPageSize()).isEqualTo(200);
        assertThat(query.getSortOptions()).satisfiesExactly(
            sort -> {
                assertThat(sort.isScore()).isTrue();
                assertThat(sort.score().order()).isEqualTo(SortOrder.Desc);
            },
            sort -> {
                assertThat(sort.isGeoDistance()).isTrue();
                assertThat(sort.geoDistance().field()).isEqualTo("placeLocation");
                assertThat(sort.geoDistance().order()).isEqualTo(SortOrder.Asc);
            }
        );
    }

    private SearchHit<LockerSuggestDocument> searchHit() {
        LockerSuggestDocument document = LockerSuggestDocument.builder()
            .lockerId(10L)
            .lockerName("신촌역 1번 출구 보관함")
            .roadAddress("서울 서대문구 신촌역로 1")
            .lockerType("SUBWAY_STATION")
            .minPrice(1000)
            .updatedAt(LocalDateTime.of(2026, 5, 31, 12, 0))
            .placeId(101L)
            .placeName("신촌역 1번 출구")
            .placeSearchNames(List.of("신촌역 1번 출구", "Sinchon Station Exit 1", "신촌 출구"))
            .lockerCount(1)
            .location(new GeoPoint(37.556, 126.923))
            .placeLocation(new GeoPoint(37.557, 126.924))
            .build();
        Map<String, List<String>> highlightFields = Map.of();
        Map<String, SearchHits<?>> innerHits = Map.of();
        Map<String, Double> matchedQueries = Map.of(
            com.zimdugo.locker.domain.LockerSuggestCandidate.PLACE_NAME_QUERY,
            10.0
        );
        return new SearchHit<LockerSuggestDocument>(
            "locker_suggest",
            "10",
            null,
            10.0F,
            new Object[] {10.0, 100.0},
            highlightFields,
            innerHits,
            null,
            null,
            matchedQueries,
            document
        );
    }
}
