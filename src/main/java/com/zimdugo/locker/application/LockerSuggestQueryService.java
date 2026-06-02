package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerSuggestQueryService {

    private final LockerSearchQueryService lockerSearchQueryService;

    public LockerSuggestResult getSuggestions(
        double latitude,
        double longitude,
        String keyword,
        int limit
    ) {
        List<LockerSuggestItemResult> items = lockerSearchQueryService.search(
            latitude,
            longitude,
            keyword,
            limit
        );
        if (items.isEmpty()) {
            return LockerSuggestResult.empty();
        }
        return LockerSuggestResult.of(items);
    }
}
