package com.zimdugo.locker.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zimdugo.locker.domain.LockerReportNameResolver;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class KakaoLocalLockerReportNameResolver implements LockerReportNameResolver {

    private static final String CATEGORY_SEARCH_PATH = "/v2/local/search/category.json";
    private static final String KEYWORD_SEARCH_PATH = "/v2/local/search/keyword.json";
    private static final int SEARCH_RADIUS_METERS = 300;
    private final RestClient restClient;

    public KakaoLocalLockerReportNameResolver(KakaoLocalApiProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeoutMillis());
        requestFactory.setReadTimeout(properties.readTimeoutMillis());

        this.restClient = RestClient.builder()
            .baseUrl(properties.baseUrl())
            .requestFactory(requestFactory)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + properties.restApiKey())
            .build();
    }

    @Override
    public String resolve(String roadAddress, String lockerType, double latitude, double longitude) {
        String placeName = searchNearestPlaceName(lockerType, latitude, longitude, roadAddress);
        if (placeName != null && !placeName.isBlank()) {
            return placeName;
        }
        if (roadAddress == null || roadAddress.isBlank()) {
            return null;
        }
        return roadAddress;
    }

    private String searchNearestPlaceName(
        String lockerType,
        double latitude,
        double longitude,
        String roadAddress
    ) {
        LockerPlaceSearchSpec spec = LockerPlaceSearchSpec.from(lockerType);

        String categoryPlaceName = searchByCategory(spec, latitude, longitude, roadAddress, lockerType);
        if (categoryPlaceName != null) {
            return categoryPlaceName;
        }

        if (spec.keyword() == null || spec.keyword().isBlank()) {
            return null;
        }
        return searchByKeyword(spec.keyword(), latitude, longitude, roadAddress, lockerType);
    }

    private String searchByCategory(
        LockerPlaceSearchSpec spec,
        double latitude,
        double longitude,
        String roadAddress,
        String lockerType
    ) {
        if (spec.categoryGroupCode() == null) {
            return null;
        }

        try {
            return extractPlaceName(
                restClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .path(CATEGORY_SEARCH_PATH)
                        .queryParam("category_group_code", spec.categoryGroupCode())
                        .queryParam("x", longitude)
                        .queryParam("y", latitude)
                        .queryParam("radius", SEARCH_RADIUS_METERS)
                        .queryParam("sort", "distance")
                        .queryParam("size", 1)
                        .build())
                    .retrieve()
                    .body(KakaoPlaceSearchResponse.class)
            );
        } catch (RestClientException e) {
            String message = String.format(
                "카카오 카테고리 API로 제보 이름을 해석하지 못했습니다. roadAddress=%s, lockerType=%s",
                roadAddress,
                lockerType
            );
            log.warn(message, e);
            return null;
        }
    }

    private String searchByKeyword(
        String keyword,
        double latitude,
        double longitude,
        String roadAddress,
        String lockerType
    ) {
        try {
            return extractPlaceName(
                restClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .path(KEYWORD_SEARCH_PATH)
                        .queryParam("query", keyword)
                        .queryParam("x", longitude)
                        .queryParam("y", latitude)
                        .queryParam("radius", SEARCH_RADIUS_METERS)
                        .queryParam("sort", "distance")
                        .queryParam("size", 1)
                        .build())
                    .retrieve()
                    .body(KakaoPlaceSearchResponse.class)
            );
        } catch (RestClientException e) {
            String message = String.format(
                "카카오 키워드 API로 제보 이름을 해석하지 못했습니다. roadAddress=%s, lockerType=%s",
                roadAddress,
                lockerType
            );
            log.warn(message, e);
            return null;
        }
    }

    private String extractPlaceName(KakaoPlaceSearchResponse response) {
        if (response == null || !response.hasPlaceName()) {
            return null;
        }
        return response.firstPlaceName();
    }

    private record KakaoPlaceSearchResponse(List<KakaoPlaceDocument> documents) {
        private boolean hasPlaceName() {
            return documents != null
                && !documents.isEmpty()
                && documents.getFirst().placeName() != null
                && !documents.getFirst().placeName().isBlank();
        }

        private String firstPlaceName() {
            return documents.getFirst().placeName();
        }
    }

    private record KakaoPlaceDocument(@JsonProperty("place_name") String placeName) {
    }

    private record LockerPlaceSearchSpec(String keyword, String categoryGroupCode) {
        private static LockerPlaceSearchSpec from(String lockerType) {
            if (lockerType == null || lockerType.isBlank()) {
                return new LockerPlaceSearchSpec(null, null);
            }

            return switch (lockerType) {
                case "SUBWAY_STATION" -> new LockerPlaceSearchSpec("지하철역", "SW8");
                case "TRAIN_STATION" -> new LockerPlaceSearchSpec("기차역", null);
                case "CONVENIENCE_STORE" -> new LockerPlaceSearchSpec("편의점", "CS2");
                case "MUSEUM" -> new LockerPlaceSearchSpec("박물관", "CT1");
                case "DEPARTMENT_STORE" -> new LockerPlaceSearchSpec("백화점", null);
                case "PUBLIC_OFFICE" -> new LockerPlaceSearchSpec("공공기관", "PO3");
                case "PRIVATE_LOCKER", "ETC" -> new LockerPlaceSearchSpec(null, null);
                default -> new LockerPlaceSearchSpec(null, null);
            };
        }
    }
}
