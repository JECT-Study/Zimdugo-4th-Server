package com.zimdugo.locker.infrastructure.realtime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.realtime.LockerRealtimeAvailabilityProvider;
import com.zimdugo.locker.domain.realtime.LockerRealtimeAvailabilitySnapshot;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class SeoulMetroLockerAvailabilityClient implements LockerRealtimeAvailabilityProvider {

    private static final int PAGE_SIZE = 1000;
    private static final int MAX_TOTAL_COUNT = 10_000;
    private static final String SUCCESS_RESULT_CODE = "00";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final int pageSize;

    public SeoulMetroLockerAvailabilityClient(RestClient restClient, ObjectMapper objectMapper, String apiKey) {
        this(restClient, objectMapper, apiKey, PAGE_SIZE);
    }

    SeoulMetroLockerAvailabilityClient(
        RestClient restClient,
        ObjectMapper objectMapper,
        String apiKey,
        int pageSize
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.pageSize = pageSize;
    }

    public List<LockerRealtimeAvailabilitySnapshot> fetchAll() {
        try {
            List<LockerRealtimeAvailabilitySnapshot> lockers = new ArrayList<>();
            Set<String> externalLockerIds = new HashSet<>();
            int startIndex = 1;
            Integer totalCount = null;
            do {
                AvailabilityPage page = fetchPage(startIndex);
                totalCount = validatePage(page, startIndex, totalCount);
                page.lockers().forEach(locker -> validateUniqueExternalLockerId(externalLockerIds, locker));
                lockers.addAll(page.lockers());
                startIndex += pageSize;
            } while (startIndex <= totalCount);
            if (lockers.size() != totalCount) {
                throw externalApiError();
            }
            return lockers;
        } catch (RestClientException | IOException ignored) {
            throw externalApiError();
        }
    }

    private AvailabilityPage fetchPage(int startIndex) throws IOException {
        int endIndex = startIndex + pageSize - 1;
        String responseBody = restClient.get()
            .uri("/{apiKey}/json/getFcLckr/{startIndex}/{endIndex}/", apiKey, startIndex, endIndex)
            .retrieve()
            .body(String.class);
        if (responseBody == null || responseBody.isBlank()) {
            throw externalApiError();
        }
        JsonNode response = objectMapper.readTree(responseBody);
        if (response == null) {
            throw externalApiError();
        }
        validateSuccess(response);

        JsonNode body = response.path("response").path("body");
        JsonNode items = body.path("items").path("item");
        if (!items.isArray()) {
            throw externalApiError();
        }
        List<LockerRealtimeAvailabilitySnapshot> lockers = new ArrayList<>();
        items.forEach(item -> lockers.add(toSnapshot(item)));
        return new AvailabilityPage(lockers, requiredTotalCount(body));
    }

    private int validatePage(AvailabilityPage page, int startIndex, Integer expectedTotalCount) {
        int totalCount = page.totalCount();
        if (totalCount > MAX_TOTAL_COUNT || expectedTotalCount != null && totalCount != expectedTotalCount) {
            throw externalApiError();
        }
        int remainingCount = Math.max(0, totalCount - startIndex + 1);
        int expectedPageSize = Math.min(pageSize, remainingCount);
        if (page.lockers().size() != expectedPageSize) {
            throw externalApiError();
        }
        return totalCount;
    }

    private void validateUniqueExternalLockerId(
        Set<String> externalLockerIds,
        LockerRealtimeAvailabilitySnapshot locker
    ) {
        if (!externalLockerIds.add(locker.externalLockerId())) {
            throw externalApiError();
        }
    }

    private void validateSuccess(JsonNode response) {
        String resultCode = response.path("response").path("header").path("resultCode").asText();
        if (!SUCCESS_RESULT_CODE.equals(resultCode)) {
            throw externalApiError();
        }
    }

    private LockerRealtimeAvailabilitySnapshot toSnapshot(JsonNode item) {
        return new LockerRealtimeAvailabilitySnapshot(
            requiredText(item, "lckrDtlId"),
            requiredCount(item, "usePsbltySmttypeLckrCnt"),
            requiredCount(item, "usePsbltyMdtypeLckrCnt"),
            requiredCount(item, "usePsbltyLrtypeLckrCnt")
        );
    }

    private String requiredText(JsonNode item, String fieldName) {
        JsonNode value = item.path(fieldName);
        if (!value.isTextual() || value.textValue().isBlank()) {
            throw externalApiError();
        }
        return value.textValue();
    }

    private int requiredCount(JsonNode item, String fieldName) {
        JsonNode value = item.path(fieldName);
        if (!value.isIntegralNumber() || !value.canConvertToInt() || value.intValue() < 0) {
            throw externalApiError();
        }
        return value.intValue();
    }

    private int requiredTotalCount(JsonNode body) {
        JsonNode value = body.path("totalCount");
        if (!value.isIntegralNumber() || !value.canConvertToInt() || value.intValue() < 0) {
            throw externalApiError();
        }
        return value.intValue();
    }

    private BusinessException externalApiError() {
        return new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
    }

    private record AvailabilityPage(List<LockerRealtimeAvailabilitySnapshot> lockers, int totalCount) {
    }
}
