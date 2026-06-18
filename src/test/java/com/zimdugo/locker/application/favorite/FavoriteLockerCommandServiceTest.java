package com.zimdugo.locker.application.favorite;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.favorite.FavoriteLockerReader;
import com.zimdugo.locker.domain.favorite.FavoriteLockerStore;
import com.zimdugo.locker.domain.locker.LockerReader;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserReader;
import com.zimdugo.user.domain.UserStatus;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FavoriteLockerCommandServiceTest {

    @Mock
    private FavoriteLockerStore favoriteLockerStore;

    @Mock
    private FavoriteLockerReader favoriteLockerReader;

    @Mock
    private LockerReader lockerReader;

    @Mock
    private UserReader userReader;

    @InjectMocks
    private FavoriteLockerCommandService favoriteLockerCommandService;

    @Test
    @DisplayName("존재하는 사용자와 보관함이면 즐겨찾기를 등록한다")
    void addFavoriteLocker() {
        given(userReader.findById(1L)).willReturn(Optional.of(activeUser(1L)));
        given(lockerReader.existsById(10L)).willReturn(true);
        given(favoriteLockerReader.exists(1L, 10L)).willReturn(false);

        favoriteLockerCommandService.add(1L, 10L);

        verify(favoriteLockerStore).save(1L, 10L);
    }

    @Test
    @DisplayName("이미 즐겨찾기된 보관함이면 중복 등록하지 않는다")
    void skipWhenAlreadyFavorite() {
        given(userReader.findById(1L)).willReturn(Optional.of(activeUser(1L)));
        given(lockerReader.existsById(10L)).willReturn(true);
        given(favoriteLockerReader.exists(1L, 10L)).willReturn(true);

        favoriteLockerCommandService.add(1L, 10L);

        verify(favoriteLockerStore, never()).save(1L, 10L);
    }

    @Test
    @DisplayName("사용자가 없으면 예외를 던진다")
    void throwWhenUserNotFound() {
        given(userReader.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteLockerCommandService.add(1L, 10L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("보관함이 없으면 예외를 던진다")
    void throwWhenLockerNotFound() {
        given(userReader.findById(1L)).willReturn(Optional.of(activeUser(1L)));
        given(lockerReader.existsById(10L)).willReturn(false);

        assertThatThrownBy(() -> favoriteLockerCommandService.add(1L, 10L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.LOCKER_NOT_FOUND);
    }

    @Test
    @DisplayName("탈퇴한 사용자는 즐겨찾기를 등록할 수 없다")
    void throwWhenUserAlreadyWithdrawnOnAdd() {
        given(userReader.findById(1L)).willReturn(Optional.of(deletedUser(1L)));

        assertThatThrownBy(() -> favoriteLockerCommandService.add(1L, 10L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_ALREADY_WITHDRAWN);
    }

    @Test
    @DisplayName("즐겨찾기 해제는 저장소에 위임한다")
    void removeFavoriteLocker() {
        given(userReader.findById(1L)).willReturn(Optional.of(activeUser(1L)));

        favoriteLockerCommandService.remove(1L, 10L);

        verify(favoriteLockerStore).delete(1L, 10L);
    }

    @Test
    @DisplayName("탈퇴한 사용자는 즐겨찾기를 해제할 수 없다")
    void throwWhenUserAlreadyWithdrawnOnRemove() {
        given(userReader.findById(1L)).willReturn(Optional.of(deletedUser(1L)));

        assertThatThrownBy(() -> favoriteLockerCommandService.remove(1L, 10L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_ALREADY_WITHDRAWN);
    }

    private User activeUser(Long id) {
        return new User(id, "user@zimdugo.com", "zimdugo", null, UserStatus.ACTIVE, null, null, null);
    }

    private User deletedUser(Long id) {
        return new User(id, "user@zimdugo.com", "zimdugo", null, UserStatus.DELETED, null, null, null);
    }
}
