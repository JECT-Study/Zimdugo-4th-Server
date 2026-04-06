package com.zimdugo.user.infrastructure;

import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.infrastructure.persistence.SocialAccountJpaEntity;
import com.zimdugo.user.infrastructure.persistence.UserJpaEntity;

final class SocialAccountEntityMapper {

    private SocialAccountEntityMapper() {
    }

    static SocialAccount toDomain(SocialAccountJpaEntity entity) {
        return new SocialAccount(
            entity.getId(),
            UserEntityMapper.toDomain(entity.getUser()),
            entity.getProvider(),
            entity.getProviderUserId(),
            entity.getProviderEmail(),
            entity.getProviderProfileImageUrl(),
            entity.getLinkedAt()
        );
    }

    static SocialAccountJpaEntity toEntity(SocialAccount socialAccount, UserJpaEntity userEntity) {
        return new SocialAccountJpaEntity(
            socialAccount.getId(),
            userEntity,
            socialAccount.getProvider(),
            socialAccount.getProviderUserId(),
            socialAccount.getProviderEmail(),
            socialAccount.getProviderProfileImageUrl(),
            socialAccount.getLinkedAt()
        );
    }
}
