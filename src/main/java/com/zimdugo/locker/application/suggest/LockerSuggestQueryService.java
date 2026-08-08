package com.zimdugo.locker.application.suggest;

import com.zimdugo.locker.application.search.LockerSearchTargetQueryService;

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

    private final LockerSearchTargetQueryService lockerSearchTargetQueryService;

    public LockerSuggestResult getSuggestions(
        double latitude,
        double longitude,
        String keyword
    ) {
        List<LockerSuggestItemResult> items = lockerSearchTargetQueryService.findTargets(
            latitude,
            longitude,
            keyword,
            null
        ).stream().map(LockerSuggestItemResult::from).toList();
        if (items.isEmpty()) {
            return LockerSuggestResult.empty();
        }
        return LockerSuggestResult.of(items);
    }
}
