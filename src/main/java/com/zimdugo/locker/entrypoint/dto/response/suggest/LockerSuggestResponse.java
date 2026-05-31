package com.zimdugo.locker.entrypoint.dto.response.suggest;

import com.zimdugo.locker.application.result.suggest.LockerSuggestResult;
import java.util.List;

public record LockerSuggestResponse(
    int count,
    List<LockerSuggestItemResponse> items
) {
    public static LockerSuggestResponse from(LockerSuggestResult result) {
        List<LockerSuggestItemResponse> items = result.items().stream()
            .map(LockerSuggestItemResponse::from)
            .toList();
        return new LockerSuggestResponse(result.count(), items);
    }
}
