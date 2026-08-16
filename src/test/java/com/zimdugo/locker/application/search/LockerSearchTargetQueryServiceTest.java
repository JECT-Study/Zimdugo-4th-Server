package com.zimdugo.locker.application.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.domain.search.LockerSearchCandidate;
import com.zimdugo.locker.domain.search.LockerSearchCandidateReader;
import com.zimdugo.locker.domain.search.LockerSearchCandidateResult;
import com.zimdugo.locker.domain.search.LockerSearchFilter;
import com.zimdugo.locker.domain.search.LockerSearchMatchSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LockerSearchTargetQueryServiceTest {

    @Mock
    private LockerSearchCandidateReader candidateReader;

    @Test
    void exposesClassifiedSearchTargetsWithoutSuggestionPresentation() {
        LockerSearchTargetQueryService service = new LockerSearchTargetQueryService(
            candidateReader,
            new LockerSearchTargetAssembler()
        );
        LockerSearchFilter filter = LockerSearchFilter.empty();
        given(candidateReader.search(37.55, 126.93, "신촌", filter, 50)).willReturn(
            LockerSearchCandidateResult.name(List.of(candidate()))
        );

        List<LockerSearchTarget> targets = service.findTargets(37.55, 126.93, "신촌", filter, 50);

        assertThat(targets).singleElement().satisfies(target -> {
            assertThat(target.type()).isEqualTo(LockerItemType.LOCKER);
            assertThat(target.lockerName()).isEqualTo("신촌역 1번 출구 보관함");
            assertThat(target.minPrice()).isEqualTo(1000);
        });
    }

    private LockerSearchCandidate candidate() {
        return new LockerSearchCandidate(
            10L, "신촌역 1번 출구 보관함", "서울 서대문구 신촌로 1", LockerType.SUBWAY_STATION,
            1000, LocalDateTime.of(2026, 8, 7, 12, 0), 100L, "신촌역 1번 출구",
            Set.of(LockerSearchMatchSource.LOCKER_NAME), 2, 100, 37.55, 126.93, 37.55, 126.93, 10.0F
        );
    }
}
