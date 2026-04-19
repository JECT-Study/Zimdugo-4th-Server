package com.zimdugo.user.application;

import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.infrastructure.SocialAccountJpaRepository;
import com.zimdugo.user.infrastructure.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserJpaRepository userJpaRepository;
    private final SocialAccountJpaRepository socialAccountJpaRepository;

    public UserProfileResponse getProfile(Long userId) {
        User user = findById(userId);

        List<SocialAccount> socialAccounts =
                socialAccountJpaRepository.findAllByUserId(userId);

        List<String> providers = socialAccounts.stream()
                .map(sa -> sa.getProvider().name().toLowerCase())
                .toList();

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getStatus().name(),
                providers
        );
    }

    // AuthController에서 재발급 시 User 조회용
    public User findById(Long userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found. id=" + userId));
    }
}
