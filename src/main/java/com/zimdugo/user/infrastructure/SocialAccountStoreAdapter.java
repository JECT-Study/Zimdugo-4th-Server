package com.zimdugo.user.infrastructure;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.SocialAccountStore;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialAccountStoreAdapter implements SocialAccountStore {

    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;

    @Override
    public SocialAccount store(SocialAccount socialAccount) {
        Long userId = socialAccount.getUser().getId();
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return SocialAccountEntityMapper.toDomain(
            socialAccountRepository.save(SocialAccountEntityMapper.toEntity(socialAccount, userEntity))
        );
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        socialAccountRepository.deleteAllByUserId(userId);
    }
}
