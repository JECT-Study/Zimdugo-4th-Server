package com.zimdugo.locker.application;

import com.zimdugo.common.util.HangulUtils;
import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.domain.LockerSearchMatchType;
import com.zimdugo.locker.domain.LockerSuggestCandidate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LockerSearchAssembler {

    public List<LockerSuggestItemResult> assemble(
        List<LockerSuggestCandidate> candidates,
        String keyword,
        int limit,
        LockerSearchMatchType matchType
    ) {
        String normalizedKeyword = normalize(keyword);
        String decomposedKeyword = HangulUtils.decompose(normalizedKeyword);
        Map<Long, LockerSuggestCandidate> bestCandidateByPlace = getBestCandidates(candidates);

        List<LockerSuggestItemResult> suggestions = new ArrayList<>(Math.min(limit, bestCandidateByPlace.size()));
        for (LockerSuggestCandidate candidate : bestCandidateByPlace.values()) {
            if (suggestions.size() >= limit) {
                break;
            }
            suggestions.add(toItemResult(candidate, normalizedKeyword, decomposedKeyword, matchType));
        }
        return suggestions;
    }

    private Map<Long, LockerSuggestCandidate> getBestCandidates(List<LockerSuggestCandidate> candidates) {
        Map<Long, LockerSuggestCandidate> bestCandidateByPlace = new LinkedHashMap<>();
        for (LockerSuggestCandidate candidate : candidates) {
            bestCandidateByPlace.putIfAbsent(candidate.placeId(), candidate);
        }
        return bestCandidateByPlace;
    }

    private LockerSuggestItemResult toItemResult(
        LockerSuggestCandidate candidate,
        String normalizedKeyword,
        String decomposedKeyword,
        LockerSearchMatchType matchType
    ) {
        String normalizedPlaceName = normalize(candidate.placeName());
        boolean placeMatched = isPlaceMatched(decomposedKeyword, normalizedPlaceName);
        boolean hasDetailKeyword = isDetailKeyword(normalizedKeyword, normalizedPlaceName);

        if (candidate.lockerCount() == 1) {
            return LockerSuggestItemResult.locker(candidate);
        }

        if (matchType == LockerSearchMatchType.ADDRESS) {
            return LockerSuggestItemResult.place(candidate);
        }

        if (hasDetailKeyword) {
            return LockerSuggestItemResult.locker(candidate);
        }

        if (placeMatched) {
            return LockerSuggestItemResult.place(candidate);
        }

        return LockerSuggestItemResult.locker(candidate);
    }

    private boolean isPlaceMatched(String decomposedKeyword, String normalizedPlaceName) {
        String decomposedPlaceName = HangulUtils.decompose(normalizedPlaceName);
        return decomposedPlaceName.contains(decomposedKeyword) || decomposedKeyword.contains(decomposedPlaceName);
    }

    private boolean isDetailKeyword(String normalizedKeyword, String normalizedPlaceName) {
        if (!normalizedKeyword.startsWith(normalizedPlaceName)) {
            return false;
        }
        return normalizedKeyword.length() > normalizedPlaceName.length();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (!Character.isWhitespace(character)) {
                builder.append(Character.toLowerCase(character));
            }
        }
        return builder.toString();
    }
}
