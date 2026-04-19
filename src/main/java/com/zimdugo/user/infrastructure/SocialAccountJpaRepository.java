package com.zimdugo.user.infrastructure;

import com.zimdugo.user.domain.AuthProvider;
import com.zimdugo.user.domain.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocialAccountJpaRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(
            AuthProvider provider, String providerUserId);

    List<SocialAccount> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
