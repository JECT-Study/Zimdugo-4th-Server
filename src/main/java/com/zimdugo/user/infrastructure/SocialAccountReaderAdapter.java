package com.zimdugo.user.infrastructure;

import com.zimdugo.identity.domain.AuthProvider;
import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.domain.SocialAccountReader;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialAccountReaderAdapter implements SocialAccountReader {

    private final SocialAccountJpaRepository socialAccountJpaRepository;

    @Override
    public Optional<SocialAccount> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
        return socialAccountJpaRepository.findByProviderAndProviderUserId(provider, providerUserId)
            .map(SocialAccountEntityMapper::toDomain);
    }

    @Override
    public List<SocialAccount> findAllByUserId(Long userId) {
        return socialAccountJpaRepository.findAllByUserId(userId).stream()
            .map(SocialAccountEntityMapper::toDomain)
            .toList();
    }
}
