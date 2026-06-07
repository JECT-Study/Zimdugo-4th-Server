package com.zimdugo.user.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserStatus;
import com.zimdugo.user.domain.UserStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileUpdateService {

    private final UserQueryService userQueryService;
    private final UserStore userStore;

    public UserProfileDto updateProfile(Long userId, String nickname, String profileImageUrl) {
        User user = userQueryService.findById(userId);
        if (user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException(ErrorCode.USER_ALREADY_WITHDRAWN);
        }

        String resolvedNickname = hasText(nickname) ? nickname : user.getNickname();
        String resolvedProfileImageUrl = hasText(profileImageUrl) ? profileImageUrl : user.getProfileImageUrl();

        user.updateProfile(resolvedNickname, resolvedProfileImageUrl);
        User updatedUser = userStore.store(user);

        return new UserProfileDto(
            updatedUser.getId(),
            updatedUser.getEmail(),
            updatedUser.getNickname(),
            updatedUser.getProfileImageUrl(),
            updatedUser.getStatus().name(),
            userQueryService.getProfile(userId).providers()
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
