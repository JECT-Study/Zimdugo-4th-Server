package com.zimdugo.user.infrastructure.adapter;

import com.zimdugo.user.infrastructure.persistence.SocialAccountRepository;

import com.zimdugo.user.domain.AuthProvider;
import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.SocialAccountReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialAccountReaderAdapter implements SocialAccountReader {

    private final SocialAccountRepository socialAccountRepository;

    @Override
    public Optional<SocialAccount> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
        return socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
            .map(SocialAccountEntityMapper::toDomain);
    }

    @Override
    public Optional<SocialAccount> findByUserId(Long userId) {
        return socialAccountRepository.findByUserId(userId)
            .map(SocialAccountEntityMapper::toDomain);
    }
}
