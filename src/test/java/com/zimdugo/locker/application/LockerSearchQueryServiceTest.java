package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.domain.LockerSearchCandidateResult;
import com.zimdugo.locker.domain.LockerSuggestCandidate;
import com.zimdugo.locker.domain.LockerSearchCandidateReader;
import com.zimdugo.locker.domain.LockerSearchFilter;
import com.zimdugo.locker.domain.LockerType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockerSearchQueryServiceTest {

    private static final LockerSearchFilter EMPTY_FILTER = LockerSearchFilter.empty();

    @Mock
    private LockerSearchCandidateReader lockerSearchCandidateReader;

    @Spy
    private LockerSearchAssembler lockerSearchAssembler;

    @InjectMocks
    private LockerSearchQueryService lockerSearchQueryService;

    @Test
    @DisplayName("주변 보관함이 없으면 빈 결과를 반환한다")
    void returnsEmptyWhenNoNearbyLockers() {
        given(lockerSearchCandidateReader.search(37.55, 126.93, "신촌", EMPTY_FILTER))
            .willReturn(LockerSearchCandidateResult.empty());

        List<LockerSuggestItemResult> items = lockerSearchQueryService.search(37.55, 126.93, "신촌");

        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("장소 키워드는 PLACE 결과로 변환한다")
    void returnsPlaceSuggestionWhenPlaceKeywordMatched() {
        List<LockerSuggestCandidate> candidates = List.of(sampleCandidate());
        given(lockerSearchCandidateReader.search(37.55, 126.93, "신촌", EMPTY_FILTER))
            .willReturn(LockerSearchCandidateResult.name(candidates));

        List<LockerSuggestItemResult> items = lockerSearchQueryService.search(37.55, 126.93, "신촌");

        assertThat(items).hasSize(1);
        LockerSuggestItemResult item = items.getFirst();
        assertThat(item.type()).isEqualTo(LockerItemType.PLACE);
        assertThat(item.placeName()).isEqualTo("신촌역 1번 출구");
        verify(lockerSearchCandidateReader).search(37.55, 126.93, "신촌", EMPTY_FILTER);
    }

    @Test
    @DisplayName("상세 키워드는 LOCKER 결과로 변환한다")
    void returnsLockerSuggestionWhenDetailKeywordMatched() {
        List<LockerSuggestCandidate> candidates = List.of(sampleCandidate());
        given(lockerSearchCandidateReader.search(37.55, 126.93, "신촌역1번출구b1", EMPTY_FILTER))
            .willReturn(LockerSearchCandidateResult.name(candidates));

        List<LockerSuggestItemResult> items = lockerSearchQueryService.search(
            37.55,
            126.93,
            "신촌역1번출구b1"
        );

        assertThat(items).hasSize(1);
        LockerSuggestItemResult item = items.getFirst();
        assertThat(item.type()).isEqualTo(LockerItemType.LOCKER);
        assertThat(item.lockerId()).isEqualTo(10L);
        assertThat(item.lockerName()).isEqualTo("신촌역 1번 출구 b1 관리사무소 옆");
        verify(lockerSearchCandidateReader).search(37.55, 126.93, "신촌역1번출구b1", EMPTY_FILTER);
    }

    @Test
    @DisplayName("주소 fallback 결과가 여러 보관함 장소이면 PLACE 결과로 변환한다")
    void returnsPlaceSuggestionWhenAddressFallbackMatchedMultipleLockers() {
        List<LockerSuggestCandidate> candidates = List.of(sampleCandidate());
        given(lockerSearchCandidateReader.search(37.55, 126.93, "대구광역시", EMPTY_FILTER))
            .willReturn(LockerSearchCandidateResult.address(candidates));

        List<LockerSuggestItemResult> items = lockerSearchQueryService.search(37.55, 126.93, "대구광역시");

        assertThat(items).hasSize(1);
        LockerSuggestItemResult item = items.getFirst();
        assertThat(item.type()).isEqualTo(LockerItemType.PLACE);
        assertThat(item.placeId()).isEqualTo(101L);
        assertThat(item.lockerId()).isNull();
        verify(lockerSearchCandidateReader).search(37.55, 126.93, "대구광역시", EMPTY_FILTER);
    }

    @Test
    @DisplayName("주소 fallback 결과가 단일 보관함 장소이면 LOCKER 결과로 변환한다")
    void returnsLockerSuggestionWhenAddressFallbackMatchedSingleLocker() {
        List<LockerSuggestCandidate> candidates = List.of(sampleCandidate(1));
        given(lockerSearchCandidateReader.search(37.55, 126.93, "대구광역시", EMPTY_FILTER))
            .willReturn(LockerSearchCandidateResult.address(candidates));

        List<LockerSuggestItemResult> items = lockerSearchQueryService.search(37.55, 126.93, "대구광역시");

        assertThat(items).hasSize(1);
        LockerSuggestItemResult item = items.getFirst();
        assertThat(item.type()).isEqualTo(LockerItemType.LOCKER);
        assertThat(item.lockerId()).isEqualTo(10L);
        verify(lockerSearchCandidateReader).search(37.55, 126.93, "대구광역시", EMPTY_FILTER);
    }

    @Test
    @DisplayName("동일한 장소의 여러 보관함이 LOCKER 타입으로 매칭되면 중복 제거 없이 모두 반환한다")
    void returnsAllLockersWithoutDeduplicationWhenMatchedAsLockerType() {
        LockerSuggestCandidate candidate1 = new LockerSuggestCandidate(
            10L,
            "신촌역 1번 출구 b1 A",
            "서울 서대문구 신촌역로 1",
            LockerType.SUBWAY_STATION,
            1000,
            LocalDateTime.now(),
            101L,
            "신촌역 1번 출구",
            2,
            100L,
            37.556,
            126.923,
            37.557,
            126.924,
            10.0F
        );
        LockerSuggestCandidate candidate2 = new LockerSuggestCandidate(
            11L,
            "신촌역 1번 출구 b1 B",
            "서울 서대문구 신촌역로 1",
            LockerType.SUBWAY_STATION,
            2000,
            LocalDateTime.now(),
            101L,
            "신촌역 1번 출구",
            2,
            100L,
            37.556,
            126.923,
            37.557,
            126.924,
            10.0F
        );
        List<LockerSuggestCandidate> candidates = List.of(candidate1, candidate2);
        given(lockerSearchCandidateReader.search(37.55, 126.93, "신촌역1번출구b1", EMPTY_FILTER))
            .willReturn(LockerSearchCandidateResult.name(candidates));

        List<LockerSuggestItemResult> items = lockerSearchQueryService.search(
            37.55,
            126.93,
            "신촌역1번출구b1"
        );

        assertThat(items).hasSize(2);
        assertThat(items.get(0).type()).isEqualTo(LockerItemType.LOCKER);
        assertThat(items.get(0).lockerId()).isEqualTo(10L);
        assertThat(items.get(1).type()).isEqualTo(LockerItemType.LOCKER);
        assertThat(items.get(1).lockerId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("좌표가 범위를 벗어나면 검색 후보를 조회하지 않는다")
    void doesNotReadCandidatesWhenLocationIsOutOfRange() {
        assertThatThrownBy(() -> lockerSearchQueryService.search(37.55, 180.1, "신촌"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_LOCATION_RANGE);

        verify(lockerSearchCandidateReader, never()).search(37.55, 180.1, "신촌", EMPTY_FILTER);
    }

    private LockerSuggestCandidate sampleCandidate() {
        return sampleCandidate(2);
    }

    private LockerSuggestCandidate sampleCandidate(int lockerCount) {
        return new LockerSuggestCandidate(
            10L,
            "신촌역 1번 출구 b1 관리사무소 옆",
            "서울 서대문구 신촌역로 1",
            LockerType.SUBWAY_STATION,
            1000,
            LocalDateTime.now(),
            101L,
            "신촌역 1번 출구",
            lockerCount,
            100L,
            37.556,
            126.923,
            37.557,
            126.924,
            10.0F
        );
    }
}
