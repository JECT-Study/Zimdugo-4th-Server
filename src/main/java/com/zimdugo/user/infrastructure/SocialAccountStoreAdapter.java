package com.zimdugo.user.infrastructure;

import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.SocialAccountStore;
import com.zimdugo.user.infrastructure.persistence.UserEntity;
import java.util.NoSuchElementException;
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
            .orElseThrow(() -> new NoSuchElementException("user not found. id=" + userId));
        return SocialAccountEntityMapper.toDomain(
            socialAccountRepository.save(SocialAccountEntityMapper.toEntity(socialAccount, userEntity))
        );
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        socialAccountRepository.deleteAllByUserId(userId);
    }
}
