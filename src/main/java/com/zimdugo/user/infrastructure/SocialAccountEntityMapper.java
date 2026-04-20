package com.zimdugo.user.infrastructure;

import com.zimdugo.user.domain.SocialAccount;
import com.zimdugo.user.infrastructure.persistence.SocialAccountEntity;
import com.zimdugo.user.infrastructure.persistence.UserEntity;

final class SocialAccountEntityMapper {

    private SocialAccountEntityMapper() {
    }

    static SocialAccount toDomain(SocialAccountEntity entity) {
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

    static SocialAccountEntity toEntity(SocialAccount socialAccount, UserEntity userEntity) {
        return new SocialAccountEntity(
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
