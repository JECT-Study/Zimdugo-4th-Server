package com.zimdugo.locker.application;

import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestType;
import com.zimdugo.locker.domain.LockerSuggestCandidate;
import com.zimdugo.locker.domain.LockerSuggestCandidateReader;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockerSuggestQueryServiceTest {

    @Mock
    private LockerSuggestCandidateReader lockerSuggestCandidateReader;

    @Spy
    private LockerSuggestAssembler lockerSuggestAssembler;

    @InjectMocks
    private LockerSuggestQueryService lockerSuggestQueryService;

    @Test
    @DisplayName("주변 보관함이 없으면 빈 결과를 반환한다")
    void returnsEmptyWhenNoNearbyLockers() {
        given(lockerSuggestCandidateReader.search(37.55, 126.93, "신촌", 10)).willReturn(List.of());

        LockerSuggestResult result = lockerSuggestQueryService.getSuggestions(37.55, 126.93, "신촌", 10);

        assertThat(result.count()).isZero();
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("장소 키워드는 PLACE 결과로 변환한다")
    void returnsPlaceSuggestionWhenPlaceKeywordMatched() {
        List<LockerSuggestCandidate> candidates = List.of(sampleCandidate());
        given(lockerSuggestCandidateReader.search(37.55, 126.93, "신촌", 10)).willReturn(candidates);

        LockerSuggestResult result = lockerSuggestQueryService.getSuggestions(37.55, 126.93, "신촌", 10);

        assertThat(result.count()).isEqualTo(1);
        LockerSuggestItemResult item = result.items().getFirst();
        assertThat(item.suggestType()).isEqualTo(LockerSuggestType.PLACE);
        assertThat(item.placeName()).isEqualTo("신촌역 1번 출구");
        verify(lockerSuggestCandidateReader).search(37.55, 126.93, "신촌", 10);
    }

    @Test
    @DisplayName("상세 키워드는 LOCKER 결과로 변환한다")
    void returnsLockerSuggestionWhenDetailKeywordMatched() {
        List<LockerSuggestCandidate> candidates = List.of(sampleCandidate());
        given(lockerSuggestCandidateReader.search(37.55, 126.93, "신촌역1번출구b1", 10)).willReturn(candidates);

        LockerSuggestResult result = lockerSuggestQueryService.getSuggestions(
            37.55,
            126.93,
            "신촌역1번출구b1",
            10
        );

        assertThat(result.count()).isEqualTo(1);
        LockerSuggestItemResult item = result.items().getFirst();
        assertThat(item.suggestType()).isEqualTo(LockerSuggestType.LOCKER);
        assertThat(item.lockerId()).isEqualTo(10L);
        assertThat(item.lockerName()).isEqualTo("신촌역 1번 출구 b1 관리사무소 옆");
        verify(lockerSuggestCandidateReader).search(37.55, 126.93, "신촌역1번출구b1", 10);
    }

    private LockerSuggestCandidate sampleCandidate() {
        return new LockerSuggestCandidate(
            10L,
            "신촌역 1번 출구 b1 관리사무소 옆",
            "서울 서대문구 신촌역로 1",
            "SUBWAY_STATION",
            LocalDateTime.now(),
            101L,
            "신촌역 1번 출구",
            2,
            100L,
            10.0F
        );
    }
}
