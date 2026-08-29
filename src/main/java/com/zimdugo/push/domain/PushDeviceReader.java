package com.zimdugo.push.domain;

import java.util.Optional;

public interface PushDeviceReader {

    Optional<Long> findIdByTokenHash(String tokenHash);
}
