package com.zimdugo.locker.application.search;

import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.domain.search.LockerSearchCandidate;
import com.zimdugo.locker.domain.search.LockerSearchMatchSource;
import com.zimdugo.locker.domain.search.LockerSearchMatchType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class LockerSearchTargetAssembler {

    public List<LockerSearchTarget> assemble(
        List<LockerSearchCandidate> candidates,
        LockerSearchMatchType matchType
    ) {
        List<LockerSearchTarget> targets = new ArrayList<>(candidates.size());
        Set<Long> seenPlaceIds = new HashSet<>();

        for (LockerSearchCandidate candidate : candidates) {
            LockerSearchTarget target = toTarget(candidate, matchType);
            if (target.type() == LockerItemType.LOCKER || seenPlaceIds.add(target.placeId())) {
                targets.add(target);
            }
        }
        return targets;
    }

    private LockerSearchTarget toTarget(
        LockerSearchCandidate candidate,
        LockerSearchMatchType matchType
    ) {
        if (candidate.lockerCount() == 1) {
            return LockerSearchTarget.locker(candidate);
        }
        if (matchType == LockerSearchMatchType.ADDRESS
            || candidate.matchSources().contains(LockerSearchMatchSource.PLACE_NAME)) {
            return LockerSearchTarget.place(candidate);
        }
        return LockerSearchTarget.locker(candidate);
    }
}
