package com.zimdugo.user.application;

import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.SocialAccountReader;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserReader;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserReader userReader;
    private final SocialAccountReader socialAccountReader;

    public UserProfileDto getProfile(Long userId) {
        User user = findById(userId);

        List<SocialAccount> socialAccounts = socialAccountReader.findAllByUserId(userId);

        List<String> providers = socialAccounts.stream()
            .map(sa -> sa.getProvider().name().toLowerCase())
            .toList();

        return new UserProfileDto(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImageUrl(),
            user.getStatus().name(),
            providers
        );
    }

    public User findById(Long userId) {
        return userReader.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
