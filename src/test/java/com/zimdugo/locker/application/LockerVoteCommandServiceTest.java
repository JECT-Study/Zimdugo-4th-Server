package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.IndoorOutdoorType;
import com.zimdugo.locker.domain.LockerDetail;
import com.zimdugo.locker.domain.LockerDetailReader;
import com.zimdugo.locker.domain.LockerDetailStore;
import com.zimdugo.locker.domain.LockerSizeType;
import com.zimdugo.locker.domain.LockerType;
import com.zimdugo.locker.domain.LockerVote;
import com.zimdugo.locker.domain.LockerVoteReader;
import com.zimdugo.locker.domain.LockerVoteStore;
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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockerVoteCommandServiceTest {

    @Mock
    private LockerVoteReader lockerVoteReader;

    @Mock
    private LockerVoteStore lockerVoteStore;

    @Mock
    private LockerDetailReader lockerDetailReader;

    @Mock
    private LockerDetailStore lockerDetailStore;

    @InjectMocks
    private LockerVoteCommandService lockerVoteCommandService;

    @Test
    @DisplayName("투표한 적이 없고 정확해요(CORRECT) 투표 시 투표를 등록하고 보관함 상세의 정확해요 카운트를 올린다")
    void toggleVoteAccurateWhenNoExistingVote() {
        LockerDetail initialDetail = createLockerDetail(10L, 5, 3);
        given(lockerDetailReader.readById(10L)).willReturn(Optional.of(initialDetail));
        given(lockerVoteReader.find(1L, 10L)).willReturn(Optional.empty());

        lockerVoteCommandService.toggleVote(1L, 10L, "CORRECT");

        verify(lockerVoteStore).save(1L, 10L, LockerVoteType.CORRECT);
        verify(lockerDetailStore).save(argThat(detail -> 
            detail.accurateVoteCount() == 6 && detail.inaccurateVoteCount() == 3
        ));
    }

    @Test
    @DisplayName("투표한 적이 없고 부정확해요(INCORRECT) 투표 시 투표를 등록하고 보관함 상세의 부정확해요 카운트를 올린다")
    void toggleVoteIncorrectWhenNoExistingVote() {
        LockerDetail initialDetail = createLockerDetail(10L, 5, 3);
        given(lockerDetailReader.readById(10L)).willReturn(Optional.of(initialDetail));
        given(lockerVoteReader.find(1L, 10L)).willReturn(Optional.empty());

        lockerVoteCommandService.toggleVote(1L, 10L, "INCORRECT");

        verify(lockerVoteStore).save(1L, 10L, LockerVoteType.INCORRECT);
        verify(lockerDetailStore).save(argThat(detail -> 
            detail.accurateVoteCount() == 5 && detail.inaccurateVoteCount() == 4
        ));
    }

    @Test
    @DisplayName("이미 동일한 정확해요(CORRECT)에 투표된 경우, 투표가 삭제(취소)되고 보관함 상세의 정확해요 카운트를 내린다")
    void toggleVoteAccurateWhenAlreadyVotedAccurate() {
        LockerDetail initialDetail = createLockerDetail(10L, 5, 3);
        given(lockerDetailReader.readById(10L)).willReturn(Optional.of(initialDetail));
        given(lockerVoteReader.find(1L, 10L)).willReturn(Optional.of(
            new LockerVote(100L, 1L, 10L, LockerVoteType.CORRECT, LocalDateTime.now())
        ));

        lockerVoteCommandService.toggleVote(1L, 10L, "CORRECT");

        verify(lockerVoteStore).delete(1L, 10L);
        verify(lockerDetailStore).save(argThat(detail -> 
            detail.accurateVoteCount() == 4 && detail.inaccurateVoteCount() == 3
        ));
    }

    @Test
    @DisplayName("이미 부정확해요(INCORRECT)에 투표된 상태에서 정확해요(CORRECT)를 누르면 투표 타입을 CORRECT로 변경하고 카운트를 조정한다")
    void toggleVoteAccurateWhenAlreadyVotedIncorrect() {
        LockerDetail initialDetail = createLockerDetail(10L, 5, 3);
        given(lockerDetailReader.readById(10L)).willReturn(Optional.of(initialDetail));
        given(lockerVoteReader.find(1L, 10L)).willReturn(Optional.of(
            new LockerVote(100L, 1L, 10L, LockerVoteType.INCORRECT, LocalDateTime.now())
        ));

        lockerVoteCommandService.toggleVote(1L, 10L, "CORRECT");

        verify(lockerVoteStore).save(1L, 10L, LockerVoteType.CORRECT);
        verify(lockerDetailStore).save(argThat(detail -> 
            detail.accurateVoteCount() == 6 && detail.inaccurateVoteCount() == 2
        ));
    }

    private LockerDetail createLockerDetail(Long lockerId, int accurateVoteCount, int inaccurateVoteCount) {
        return new LockerDetail(
            lockerId, "보관함", "주소", 37.5, 127.0, 1L, "장소",
            LockerType.ETC, IndoorOutdoorType.INDOOR, "1", 1, 1000, 2000,
            Set.of(LockerSizeType.SMALL), "정보", LocalTime.of(9, 0), LocalTime.of(22, 0),
            "image.jpg", accurateVoteCount, inaccurateVoteCount,
            LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
