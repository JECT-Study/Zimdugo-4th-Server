package com.zimdugo.locker.entrypoint.dto.response.keyword;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zimdugo.locker.application.result.keyword.LockerKeywordResult;
import com.zimdugo.locker.entrypoint.dto.response.LockerBoundsResponse;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LockerKeywordResponse(
    int count,
    LockerBoundsResponse bounds,
    List<LockerKeywordItemResponse> items
) {
    public static LockerKeywordResponse from(LockerKeywordResult result) {
        List<LockerKeywordItemResponse> items = result.items().stream()
            .map(LockerKeywordItemResponse::from)
            .toList();
        return new LockerKeywordResponse(
            result.count(),
            LockerBoundsResponse.from(result.bounds()),
            items
        );
    }
}
