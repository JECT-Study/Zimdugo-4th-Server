package com.zimdugo.locker.application.search;

import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.domain.search.LockerSearchMatchType;
import com.zimdugo.locker.domain.search.LockerSuggestCandidate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class LockerSearchAssembler {

    public List<LockerSuggestItemResult> assemble(
        List<LockerSuggestCandidate> candidates,
        LockerSearchMatchType matchType
    ) {
        List<LockerSuggestItemResult> suggestions = new ArrayList<>(candidates.size());
        Set<Long> seenPlaceIdsForPlaceType = new HashSet<>();

        for (LockerSuggestCandidate candidate : candidates) {
            LockerSuggestItemResult item = toItemResult(candidate, matchType);
            if (item.type() == LockerItemType.LOCKER) {
                suggestions.add(item);
            } else if (item.type() == LockerItemType.PLACE) {
                if (seenPlaceIdsForPlaceType.add(candidate.placeId())) {
                    suggestions.add(item);
                }
            }
        }
        return suggestions;
    }

    private LockerSuggestItemResult toItemResult(
        LockerSuggestCandidate candidate,
        LockerSearchMatchType matchType
    ) {
        if (candidate.lockerCount() == 1) {
            return LockerSuggestItemResult.locker(candidate);
        }

        if (matchType == LockerSearchMatchType.ADDRESS) {
            return LockerSuggestItemResult.place(candidate);
        }

        if (candidate.matchedQueries().contains(LockerSuggestCandidate.PLACE_NAME_QUERY)) {
            return LockerSuggestItemResult.place(candidate);
        }

        if (candidate.matchedQueries().contains(LockerSuggestCandidate.LOCKER_NAME_QUERY)) {
            return LockerSuggestItemResult.locker(candidate);
        }

        return LockerSuggestItemResult.locker(candidate);
    }
}
