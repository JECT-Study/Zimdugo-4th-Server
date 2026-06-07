package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.domain.LockerSearchCandidateResult;
import com.zimdugo.locker.domain.LockerSearchCandidateReader;
import com.zimdugo.locker.domain.LockerSearchFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerSearchQueryService {

    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    private final LockerSearchCandidateReader lockerSearchCandidateReader;
    private final LockerSearchAssembler lockerSearchAssembler;

    public List<LockerSuggestItemResult> search(
        double latitude,
        double longitude,
        String keyword,
        int limit
    ) {
        return search(latitude, longitude, keyword, limit, LockerSearchFilter.empty());
    }

    public List<LockerSuggestItemResult> search(
        double latitude,
        double longitude,
        String keyword,
        int limit,
        LockerSearchFilter filter
    ) {
        validateInputs(latitude, longitude, keyword);

        LockerSearchCandidateResult candidateResult = lockerSearchCandidateReader.search(
            latitude,
            longitude,
            keyword,
            limit,
            filter
        );
        if (candidateResult.candidates().isEmpty()) {
            return List.of();
        }

        return lockerSearchAssembler.assemble(
            candidateResult.candidates(),
            keyword,
            limit,
            candidateResult.matchType()
        );
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
