package com.zimdugo.auth.domain;

import com.zimdugo.user.domain.AuthProvider;
import java.util.Optional;

public interface SocialProviderTokenRepository {

    void save(Long userId, AuthProvider provider, SocialProviderToken token);

    Optional<SocialProviderToken> find(Long userId, AuthProvider provider);

    void deleteAllByUserId(Long userId);
}
