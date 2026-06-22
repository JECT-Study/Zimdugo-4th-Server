package com.zimdugo.admin.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

class KakaoPlaceCandidateClientTest {

    @Test
    void searchesAllKakaoCategoryGroups() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dapi.kakao.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        List<String> requestedCategoryCodes = new ArrayList<>();
        server.expect(manyTimes(), request -> requestedCategoryCodes.add(
                UriComponentsBuilder.fromUri(request.getURI())
                    .build()
                    .getQueryParams()
                    .getFirst("category_group_code")
            ))
            .andRespond(withSuccess("{\"documents\": []}", MediaType.APPLICATION_JSON));
        KakaoPlaceCandidateClient client = new KakaoPlaceCandidateClient(builder.build(), "test-key");

        client.findNearby(37.55, 126.97);

        assertThat(requestedCategoryCodes).containsExactly(
            "MT1", "CS2", "PS3", "SC4", "AC5", "PK6", "OL7", "SW8", "BK9",
            "CT1", "AG2", "PO3", "AT4", "AD5", "FD6", "CE7", "HP8", "PM9"
        );
        server.verify();
    }

    @Test
    void mergesCategoryResultsByIdAndSortsByDistance() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dapi.kakao.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(manyTimes(), request -> {
                assertThat(request.getURI().getPath()).isEqualTo("/v2/local/search/category.json");
                assertThat(request.getURI().getQuery()).contains("radius=30");
            })
            .andRespond(withSuccess(responseJson(), MediaType.APPLICATION_JSON));
        KakaoPlaceCandidateClient client = new KakaoPlaceCandidateClient(builder.build(), "test-key");

        List<KakaoPlaceCandidate> candidates = client.findNearby(37.55, 126.97);

        assertThat(candidates).extracting(KakaoPlaceCandidate::id)
            .containsExactly("near");
        assertThat(candidates.getFirst().name()).isEqualTo("서울역");
        assertThat(candidates.getFirst().roadAddress()).isEqualTo("서울 중구 한강대로 405");
        server.verify();
    }

    private String responseJson() {
        return """
            {
              "documents": [
                {
                  "id": "far",
                  "place_name": "서울역 광장",
                  "category_name": "관광명소 > 광장",
                  "road_address_name": "서울 중구 통일로 1",
                  "address_name": "서울 중구 봉래동2가",
                  "x": "126.971",
                  "y": "37.556",
                  "distance": "80",
                  "place_url": "https://place.map.kakao.com/far"
                },
                {
                  "id": "near",
                  "place_name": "서울역",
                  "category_name": "교통 > 철도역",
                  "road_address_name": "서울 중구 한강대로 405",
                  "address_name": "서울 중구 봉래동2가 122",
                  "x": "126.9707",
                  "y": "37.5547",
                  "distance": "12",
                  "place_url": "https://place.map.kakao.com/near"
                }
              ]
            }
            """;
    }
}
