package com.zimdugo.locker.application.search;

import com.zimdugo.locker.application.common.LocationValidator;

import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.domain.search.LockerSearchCandidateReader;
import com.zimdugo.locker.domain.search.LockerSearchCandidateResult;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LockerSearchQueryService {

    private final LockerSearchCandidateReader lockerSearchCandidateReader;
    private final LockerSearchAssembler lockerSearchAssembler;

    public List<LockerSuggestItemResult> search(
        double latitude,
        double longitude,
        String keyword
    ) {
        return search(latitude, longitude, keyword, LockerSearchFilter.empty());
    }

    public List<LockerSuggestItemResult> search(
        double latitude,
        double longitude,
        String keyword,
        LockerSearchFilter filter
    ) {
        LocationValidator.validate(latitude, longitude);

        if (filter == null) {
            filter = LockerSearchFilter.empty();
        }
        LockerSearchCandidateResult candidateResult = lockerSearchCandidateReader.search(
            latitude,
            longitude,
            keyword,
            filter
        );
        if (candidateResult.candidates().isEmpty()) {
            return List.of();
        }

        return lockerSearchAssembler.assemble(
            candidateResult.candidates(),
            candidateResult.matchType()
        );
    }


}
