package com.zimdugo.locker.entrypoint.dto.response.keyword;

import com.zimdugo.locker.application.result.keyword.LockerKeywordResult;
import java.util.List;

public record LockerKeywordResponse(
    int count,
    List<LockerKeywordItemResponse> items
) {
    public static LockerKeywordResponse from(LockerKeywordResult result) {
        List<LockerKeywordItemResponse> items = result.items().stream()
            .map(LockerKeywordItemResponse::from)
            .toList();
        return new LockerKeywordResponse(result.count(), items);
    }
}
