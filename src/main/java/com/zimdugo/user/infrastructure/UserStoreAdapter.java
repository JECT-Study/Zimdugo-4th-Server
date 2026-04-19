package com.zimdugo.user.infrastructure;

import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserStore;
import com.zimdugo.user.infrastructure.persistence.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserStoreAdapter implements UserStore {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User store(User user) {
        UserJpaEntity saved = userJpaRepository.save(UserEntityMapper.toEntity(user));
        return UserEntityMapper.toDomain(saved);
    }
}
