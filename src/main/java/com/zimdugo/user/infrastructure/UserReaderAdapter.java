package com.zimdugo.user.infrastructure;

import com.zimdugo.user.domain.User;
import com.zimdugo.user.domain.UserReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserReaderAdapter implements UserReader {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id).map(UserEntityMapper::toDomain);
    }
}
