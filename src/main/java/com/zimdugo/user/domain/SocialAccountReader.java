package com.zimdugo.user.domain;

import com.zimdugo.identity.domain.AuthProvider;
import java.util.List;
import java.util.Optional;

public interface SocialAccountReader {

    Optional<SocialAccount> findByProviderAndProviderUserId(
        AuthProvider provider,
        String providerUserId
    );

    List<SocialAccount> findAllByUserId(Long userId);
}
