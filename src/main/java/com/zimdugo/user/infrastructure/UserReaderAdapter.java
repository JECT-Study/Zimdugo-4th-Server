package com.zimdugo.user.infrastructure;

import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserReaderAdapter implements UserReader {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id).map(UserEntityMapper::toDomain);
    }
}
