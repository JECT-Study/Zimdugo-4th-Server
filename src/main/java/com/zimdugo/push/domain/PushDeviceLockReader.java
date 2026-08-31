package com.zimdugo.push.domain;

import java.util.Optional;

public interface PushDeviceLockReader {

    Optional<Long> findIdByTokenHashForUpdate(String tokenHash);
}
