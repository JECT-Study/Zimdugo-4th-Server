package com.zimdugo.user.infrastructure;

import com.zimdugo.identity.domain.AuthProvider;
import com.zimdugo.user.infrastructure.persistence.SocialAccountEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialAccountRepository extends JpaRepository<SocialAccountEntity, Long> {

    Optional<SocialAccountEntity> findByProviderAndProviderUserId(
        AuthProvider provider,
        String providerUserId
    );

    List<SocialAccountEntity> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
