package com.zimdugo.locker.application.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.search.LockerSearchCandidate;
import com.zimdugo.locker.domain.search.LockerSearchMatchSource;
import com.zimdugo.locker.domain.search.LockerSearchMatchType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LockerSearchTargetAssemblerTest {

    private final LockerSearchTargetAssembler assembler = new LockerSearchTargetAssembler();

    @Test
    void returnsOnePlaceTargetWhenMultipleLockersMatchTheSamePlaceName() {
        List<LockerSearchTarget> targets = assembler.assemble(
            List.of(candidate(10L, 100L, 2, Set.of(LockerSearchMatchSource.PLACE_NAME)),
                candidate(11L, 100L, 2, Set.of(LockerSearchMatchSource.PLACE_NAME))),
            LockerSearchMatchType.NAME
        );

        assertThat(targets)
            .extracting(LockerSearchTarget::type, LockerSearchTarget::placeId, LockerSearchTarget::lockerId)
            .containsExactly(tuple(LockerItemType.PLACE, 100L, null));
    }

    @Test
    void returnsALockerTargetWhenALockerNameMatchesInAPlaceWithMultipleLockers() {
        List<LockerSearchTarget> targets = assembler.assemble(
            List.of(candidate(10L, 100L, 2, Set.of(LockerSearchMatchSource.LOCKER_NAME))),
            LockerSearchMatchType.NAME
        );

        assertThat(targets)
            .extracting(LockerSearchTarget::type, LockerSearchTarget::lockerId)
            .containsExactly(tuple(LockerItemType.LOCKER, 10L));
    }

    @Test
    void returnsALockerTargetForAnAddressMatchAtASingleLockerPlace() {
        List<LockerSearchTarget> targets = assembler.assemble(
            List.of(candidate(10L, 100L, 1, Set.of())),
            LockerSearchMatchType.ADDRESS
        );

        assertThat(targets)
            .extracting(LockerSearchTarget::type, LockerSearchTarget::placeId, LockerSearchTarget::lockerId)
            .containsExactly(tuple(LockerItemType.LOCKER, 100L, 10L));
    }

    private LockerSearchCandidate candidate(
        Long lockerId,
        Long placeId,
        int lockerCount,
        Set<LockerSearchMatchSource> matchSources
    ) {
        return new LockerSearchCandidate(
            lockerId,
            "보관함 " + lockerId,
            "서울특별시",
            LockerType.SUBWAY_STATION,
            1000,
            LocalDateTime.of(2026, 8, 7, 12, 0),
            placeId,
            "장소 " + placeId,
            matchSources,
            lockerCount,
            100,
            37.55,
            126.93,
            37.55,
            126.93,
            10.0F
        );
    }
}
