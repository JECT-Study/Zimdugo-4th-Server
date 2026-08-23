package com.zimdugo.user.domain;
import java.util.List;
import java.util.Optional;

public interface SocialAccountReader {

    Optional<SocialAccount> findByProviderAndProviderUserId(
        AuthProvider provider,
        String providerUserId
    );

    Optional<SocialAccount> findByUserId(Long userId);

    List<SocialAccount> findAllByUserId(Long userId);
}
