package com.zimdugo.user.infrastructure;

import com.zimdugo.user.infrastructure.persistence.User;

final class UserEntityMapper {

    private UserEntityMapper() {
    }

    static com.zimdugo.user.domain.User toDomain(User entity) {
        return new com.zimdugo.user.domain.User(
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

    static User toEntity(com.zimdugo.user.domain.User user) {
        return new User(
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
