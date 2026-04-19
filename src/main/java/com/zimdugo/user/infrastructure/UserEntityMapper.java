package com.zimdugo.user.infrastructure;

import com.zimdugo.user.domain.User;
import com.zimdugo.user.infrastructure.persistence.UserEntity;

final class UserEntityMapper {

    private UserEntityMapper() {
    }

    static User toDomain(UserEntity entity) {
        return new User(
            entity.getId(),
            entity.getEmail(),
            entity.getNickname(),
            entity.getProfileImageUrl(),
            entity.getStatus(),
            entity.getRole(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static UserEntity toEntity(User user) {
        return new UserEntity(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImageUrl(),
            user.getStatus(),
            user.getRoleOrDefault(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
