package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.result.detail.LockerDetailResult;
import com.zimdugo.locker.domain.FavoriteLockerReader;
import com.zimdugo.locker.domain.IndoorOutdoorType;
import com.zimdugo.locker.domain.LockerDetail;
import com.zimdugo.locker.domain.LockerDetailReader;
import com.zimdugo.locker.domain.LockerSizeType;
import com.zimdugo.locker.domain.LockerType;
import com.zimdugo.locker.domain.LockerVoteReader;
import com.zimdugo.locker.domain.LockerVoteType;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LockerDetailQueryServiceTest {

    @Mock
    private LockerDetailReader lockerDetailReader;

    @Mock
    private FavoriteLockerReader favoriteLockerReader;

    @Mock
    private LockerVoteReader lockerVoteReader;

    @InjectMocks
    private LockerDetailQueryService lockerDetailQueryService;

    @Test
    @DisplayName("비로그인 사용자가 보관함 상세정보를 조회하면 즐겨찾기 및 투표 상태가 false로 반환된다")
    void returnsDetailForGuest() {
        given(lockerDetailReader.readById(10L)).willReturn(Optional.of(detail()));

        LockerDetailResult result = lockerDetailQueryService.getDetail(null, 10L);

        assertThat(result.lockerId()).isEqualTo(10L);
        assertThat(result.lockerName()).isEqualTo("신촌역 보관함");
        assertThat(result.placeId()).isEqualTo(101L);
        assertThat(result.lockerType()).isEqualTo("SUBWAY_STATION");
        assertThat(result.isFavorite()).isFalse();
        assertThat(result.isAccurateVoted()).isFalse();
        assertThat(result.isInaccurateVoted()).isFalse();
    }

    @Test
    @DisplayName("로그인 사용자가 보관함 상세정보를 조회하면 즐겨찾기 및 본인의 투표 여부를 포함해 반환한다")
    void returnsDetailForUser() {
        given(lockerDetailReader.readById(10L)).willReturn(Optional.of(detail()));
        given(favoriteLockerReader.exists(1L, 10L)).willReturn(true);
        given(lockerVoteReader.exists(1L, 10L, LockerVoteType.CORRECT)).willReturn(true);
        given(lockerVoteReader.exists(1L, 10L, LockerVoteType.INCORRECT)).willReturn(false);

        LockerDetailResult result = lockerDetailQueryService.getDetail(1L, 10L);

        assertThat(result.lockerId()).isEqualTo(10L);
        assertThat(result.isFavorite()).isTrue();
        assertThat(result.isAccurateVoted()).isTrue();
        assertThat(result.isInaccurateVoted()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 보관함이면 404 예외를 발생시킨다")
    void throwsWhenLockerDoesNotExist() {
        given(lockerDetailReader.readById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> lockerDetailQueryService.getDetail(null, 999L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.LOCKER_NOT_FOUND);
    }

    private LockerDetail detail() {
        return new LockerDetail(
            10L,
            "신촌역 보관함",
            "서울 서대문구",
            37.55,
            126.93,
            101L,
            "신촌역",
            LockerType.SUBWAY_STATION,
            IndoorOutdoorType.INDOOR,
            "UNDERGROUND",
            -1,
            1000,
            3000,
            Set.of(LockerSizeType.SMALL, LockerSizeType.LARGE),
            "개찰구 옆",
            LocalTime.of(9, 0),
            LocalTime.of(22, 0),
            "https://cdn.example.com/locker.jpg",
            10,
            2,
            LocalDateTime.of(2026, 6, 1, 12, 0),
            LocalDateTime.of(2026, 6, 7, 12, 0)
        );
    }
}
