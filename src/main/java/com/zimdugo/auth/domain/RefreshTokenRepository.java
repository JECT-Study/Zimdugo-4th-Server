package com.zimdugo.auth.domain;

import java.time.Duration;

public interface RefreshTokenRepository {

    void save(Long userId, String sid, String rawToken, Duration ttl);

    boolean matches(Long userId, String sid, String rawToken);

    void delete(Long userId, String sid);

    void deleteAllByUserId(Long userId);
}
