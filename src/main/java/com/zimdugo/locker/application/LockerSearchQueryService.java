package com.zimdugo.locker.application;

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
            keyword,
            candidateResult.matchType()
        );
    }
}
