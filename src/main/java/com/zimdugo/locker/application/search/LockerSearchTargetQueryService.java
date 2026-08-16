package com.zimdugo.locker.application.search;

import com.zimdugo.locker.application.common.LocationValidator;
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
public class LockerSearchTargetQueryService {

    private final LockerSearchCandidateReader lockerSearchCandidateReader;
    private final LockerSearchTargetAssembler lockerSearchTargetAssembler;

    public List<LockerSearchTarget> findTargets(
        double latitude,
        double longitude,
        String keyword,
        LockerSearchFilter filter,
        int limit
    ) {
        LocationValidator.validate(latitude, longitude);
        LockerSearchFilter effectiveFilter = filter == null ? LockerSearchFilter.empty() : filter;
        LockerSearchCandidateResult candidateResult = lockerSearchCandidateReader.search(
            latitude,
            longitude,
            keyword,
            effectiveFilter,
            limit
        );
        if (candidateResult.candidates().isEmpty()) {
            return List.of();
        }

        List<LockerSearchTarget> targets = lockerSearchTargetAssembler.assemble(
            candidateResult.candidates(),
            candidateResult.matchType()
        );
        log.debug(
            "보관함 검색 대상 조회 완료. keywordPresent={}, filterEmpty={}, matchType={}, targetCount={}",
            keyword != null && !keyword.isBlank(),
            effectiveFilter.isEmpty(),
            candidateResult.matchType(),
            targets.size()
        );
        return targets;
    }
}
