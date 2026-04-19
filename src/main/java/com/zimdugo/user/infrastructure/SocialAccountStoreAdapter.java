package com.zimdugo.user.infrastructure;

import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.SocialAccountStore;
import com.zimdugo.user.infrastructure.persistence.UserJpaEntity;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialAccountStoreAdapter implements SocialAccountStore {

    private final SocialAccountJpaRepository socialAccountJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public SocialAccount store(SocialAccount socialAccount) {
        Long userId = socialAccount.getUser().getId();
        UserJpaEntity userEntity = userJpaRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("user not found. id=" + userId));
        return SocialAccountEntityMapper.toDomain(
            socialAccountJpaRepository.save(SocialAccountEntityMapper.toEntity(socialAccount, userEntity))
        );
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        socialAccountJpaRepository.deleteAllByUserId(userId);
    }
}
