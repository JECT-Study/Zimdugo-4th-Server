package com.zimdugo.locker.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zimdugo.locker.domain.LockerReportNameResolver;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class KakaoLocalLockerReportNameResolver implements LockerReportNameResolver {

    private static final String CATEGORY_SEARCH_PATH = "/v2/local/search/category.json";
    private static final String KEYWORD_SEARCH_PATH = "/v2/local/search/keyword.json";
    private static final int SEARCH_RADIUS_METERS = 300;

    private final RestClient restClient;

    public KakaoLocalLockerReportNameResolver(KakaoLocalApiProperties properties) {
        this.restClient = RestClient.builder()
            .baseUrl(properties.baseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + properties.restApiKey())
            .build();
    }

    @Override
    public String resolve(String roadAddress, String lockerType, double latitude, double longitude) {
        try {
            String placeName = searchNearestPlaceName(lockerType, latitude, longitude);
            if (placeName != null && !placeName.isBlank()) {
                return placeName;
            }
        } catch (Exception e) {
            log.warn(
                "카카오 Local API로 제보 장소명을 찾지 못했습니다. roadAddress={}, lockerType={}",
                roadAddress,
                lockerType,
                e
            );
        }

        if (roadAddress == null || roadAddress.isBlank()) {
            return null;
        }
        return roadAddress;
    }

    private String searchNearestPlaceName(String lockerType, double latitude, double longitude) {
        LockerPlaceSearchSpec spec = LockerPlaceSearchSpec.from(lockerType);

        String categoryPlaceName = searchByCategory(spec, latitude, longitude);
        if (categoryPlaceName != null) {
            return categoryPlaceName;
        }

        return searchByKeyword(spec.keyword(), latitude, longitude);
    }

    private String searchByCategory(LockerPlaceSearchSpec spec, double latitude, double longitude) {
        if (spec.categoryGroupCode() == null) {
            return null;
        }

        KakaoPlaceSearchResponse response = restClient.get()
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
            .body(KakaoPlaceSearchResponse.class);

        if (response == null || !response.hasPlaceName()) {
            return null;
        }
        return response.firstPlaceName();
    }

    private String searchByKeyword(String keyword, double latitude, double longitude) {
        KakaoPlaceSearchResponse response = restClient.get()
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
            .body(KakaoPlaceSearchResponse.class);

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
                return new LockerPlaceSearchSpec("물품보관함", null);
            }

            return switch (lockerType) {
                case "SUBWAY_STATION" -> new LockerPlaceSearchSpec("지하철역", "SW8");
                case "TRAIN_STATION" -> new LockerPlaceSearchSpec("기차역", null);
                case "CONVENIENCE_STORE" -> new LockerPlaceSearchSpec("편의점", "CS2");
                case "MUSEUM" -> new LockerPlaceSearchSpec("박물관", "CT1");
                case "DEPARTMENT_STORE" -> new LockerPlaceSearchSpec("백화점", null);
                case "PUBLIC_OFFICE" -> new LockerPlaceSearchSpec("공공기관", "PO3");
                case "PRIVATE_LOCKER" -> new LockerPlaceSearchSpec("물품보관함", null);
                default -> new LockerPlaceSearchSpec("물품보관함", null);
            };
        }
    }
}
