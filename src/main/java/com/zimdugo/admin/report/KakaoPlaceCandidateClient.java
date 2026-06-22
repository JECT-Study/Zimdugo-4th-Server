package com.zimdugo.admin.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.infrastructure.config.KakaoLocalApiProperties;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KakaoPlaceCandidateClient implements PlaceCandidateProvider {

    private static final int SEARCH_RADIUS_METERS = 30;
    private static final int CATEGORY_RESULT_SIZE = 5;
    private static final int MAX_CANDIDATES = 15;
    private static final List<String> CATEGORY_CODES = List.of(
        "MT1", "CS2", "PS3", "SC4", "AC5", "PK6", "OL7", "SW8", "BK9",
        "CT1", "AG2", "PO3", "AT4", "AD5", "FD6", "CE7", "HP8", "PM9"
    );

    private final RestClient restClient;
    private final String restApiKey;

    @Autowired
    public KakaoPlaceCandidateClient(
        @Qualifier("kakaoLocalRestClient") RestClient restClient,
        KakaoLocalApiProperties properties
    ) {
        this(restClient, properties.restApiKey());
    }

    KakaoPlaceCandidateClient(RestClient restClient, String restApiKey) {
        this.restClient = restClient;
        this.restApiKey = restApiKey;
    }

    @Override
    public List<KakaoPlaceCandidate> findNearby(double latitude, double longitude) {
        try {
            Map<String, KakaoPlaceCandidate> candidatesById = new LinkedHashMap<>();
            for (String categoryCode : CATEGORY_CODES) {
                searchCategory(categoryCode, latitude, longitude).stream()
                    .map(Document::toCandidate)
                    .forEach(candidate -> candidatesById.putIfAbsent(candidate.id(), candidate));
            }
            return candidatesById.values().stream()
                .filter(candidate -> candidate.distanceMeters() <= SEARCH_RADIUS_METERS)
                .sorted(Comparator.comparingInt(KakaoPlaceCandidate::distanceMeters))
                .limit(MAX_CANDIDATES)
                .toList();
        } catch (RestClientException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, exception);
        }
    }

    private List<Document> searchCategory(String categoryCode, double latitude, double longitude) {
        SearchResponse response = restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v2/local/search/category.json")
                .queryParam("category_group_code", categoryCode)
                .queryParam("x", longitude)
                .queryParam("y", latitude)
                .queryParam("radius", SEARCH_RADIUS_METERS)
                .queryParam("size", CATEGORY_RESULT_SIZE)
                .queryParam("sort", "distance")
                .build())
            .header("Authorization", "KakaoAK " + restApiKey)
            .retrieve()
            .body(SearchResponse.class);
        return response == null || response.documents() == null ? List.of() : response.documents();
    }

    private record SearchResponse(List<Document> documents) {
    }

    private record Document(
        String id,
        @JsonProperty("place_name") String placeName,
        @JsonProperty("category_name") String categoryName,
        @JsonProperty("road_address_name") String roadAddressName,
        @JsonProperty("address_name") String addressName,
        String x,
        String y,
        String distance,
        @JsonProperty("place_url") String placeUrl
    ) {
        KakaoPlaceCandidate toCandidate() {
            String address = roadAddressName == null || roadAddressName.isBlank()
                ? addressName
                : roadAddressName;
            return new KakaoPlaceCandidate(
                id,
                placeName,
                categoryName,
                address,
                Double.parseDouble(y),
                Double.parseDouble(x),
                Integer.parseInt(distance),
                placeUrl
            );
        }
    }
}
