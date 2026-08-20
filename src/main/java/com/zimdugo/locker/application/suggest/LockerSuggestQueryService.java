package com.zimdugo.locker.application.suggest;

import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestResult;
import com.zimdugo.locker.application.search.LockerSearchTargetQueryService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LockerSuggestQueryService {

    private final LockerSearchTargetQueryService lockerSearchTargetQueryService;
    private final int suggestLimit;

    public LockerSuggestQueryService(
        LockerSearchTargetQueryService lockerSearchTargetQueryService,
        @Value("${search.suggest-limit}") int suggestLimit
    ) {
        this.lockerSearchTargetQueryService = lockerSearchTargetQueryService;
        this.suggestLimit = suggestLimit;
    }

    public LockerSuggestResult getSuggestions(
        double latitude,
        double longitude,
        String keyword
    ) {
        List<LockerSuggestItemResult> items = lockerSearchTargetQueryService.findTargets(
            latitude,
            longitude,
            keyword,
            null,
            suggestLimit
        ).stream().map(LockerSuggestItemResult::from).toList();
        if (items.isEmpty()) {
            return LockerSuggestResult.empty();
        }
        return LockerSuggestResult.of(items);
    }
}
