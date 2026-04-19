package com.zimdugo.user.infrastructure;

import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserStoreAdapter implements UserStore {

    private final UserRepository userRepository;

    @Override
    public User store(User user) {
        return UserEntityMapper.toDomain(
            userRepository.save(UserEntityMapper.toEntity(user))
        );
    }
}
