package com.zimdugo.user.infrastructure;

import com.zimdugo.identity.domain.AuthProvider;
import com.zimdugo.user.infrastructure.persistence.SocialAccountJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialAccountJpaRepository extends JpaRepository<SocialAccountJpaEntity, Long> {

    Optional<SocialAccountJpaEntity> findByProviderAndProviderUserId(
        AuthProvider provider,
        String providerUserId
    );

    List<SocialAccountJpaEntity> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
