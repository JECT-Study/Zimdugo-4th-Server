package com.zimdugo.user.domain;

import java.util.Optional;

public interface UserReader {

    Optional<User> findById(Long id);
}
