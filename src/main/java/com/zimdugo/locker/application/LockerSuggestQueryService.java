package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestResult;
import com.zimdugo.locker.domain.LockerSuggestCandidate;
import com.zimdugo.locker.domain.LockerSuggestCandidateReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerSuggestQueryService {

    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    private final LockerSuggestCandidateReader lockerSuggestCandidateReader;
    private final LockerSuggestAssembler lockerSuggestAssembler;

    public LockerSuggestResult getSuggestions(
        double latitude,
        double longitude,
        String keyword,
        int limit
    ) {
        validateInputs(latitude, longitude, keyword);

        List<LockerSuggestCandidate> candidates = lockerSuggestCandidateReader.search(
            latitude,
            longitude,
            keyword,
            limit
        );
        if (candidates.isEmpty()) {
            return LockerSuggestResult.empty();
        }

        List<LockerSuggestItemResult> suggestions = lockerSuggestAssembler.assemble(candidates, keyword, limit);
        return LockerSuggestResult.of(suggestions);
    }

    private void validateInputs(double lat, double lon, String keyword) {
        if (lat < MIN_LATITUDE || lat > MAX_LATITUDE || lon < MIN_LONGITUDE || lon > MAX_LONGITUDE) {
            throw new BusinessException(ErrorCode.INVALID_LOCATION_RANGE);
        }

        if (keyword == null || keyword.trim().length() < 2) {
            throw new BusinessException(ErrorCode.SEARCH_KEYWORD_TOO_SHORT);
        }
    }
}
