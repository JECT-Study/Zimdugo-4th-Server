package com.zimdugo.locker.application.search;

import com.zimdugo.locker.application.common.LocationValidator;

import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.domain.search.LockerSearchCandidateReader;
import com.zimdugo.locker.domain.search.LockerSearchCandidateResult;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
        log.debug(
            "보관함 검색 후보 조회 완료. keywordPresent={}, filterEmpty={}, matchType={}, resultCount={}",
            keyword != null && !keyword.isBlank(),
            filter.isEmpty(),
            candidateResult.matchType(),
            candidateResult.candidates().size()
        );
        if (candidateResult.candidates().isEmpty()) {
            return List.of();
        }

        List<LockerSuggestItemResult> results = lockerSearchAssembler.assemble(
            candidateResult.candidates(),
            candidateResult.matchType()
        );
        log.debug("보관함 검색 응답 생성 완료. resultCount={}", results.size());
        return results;
    }


}
