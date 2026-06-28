package com.zimdugo.locker.entrypoint.dto.response.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zimdugo.locker.application.result.search.LockerSearchResult;
import com.zimdugo.locker.entrypoint.dto.response.LockerBoundsResponse;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LockerSearchResponse(
    int count,
    LockerBoundsResponse bounds,
    List<LockerSearchItemResponse> items
) {
    public static LockerSearchResponse from(LockerSearchResult result) {
        List<LockerSearchItemResponse> items = result.items().stream()
            .map(LockerSearchItemResponse::from)
            .toList();
        return new LockerSearchResponse(
            result.count(),
            LockerBoundsResponse.from(result.bounds()),
            items
        );
    }
}
